package com.ctk.orchestrator.service;

import com.ctk.orchestrator.api.dto.EventIngestRequest;
import com.ctk.orchestrator.model.*;
import com.ctk.orchestrator.repo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class OrchestrationService {
  private final IdempotencyService idempotency;
  private final ObjectMapper mapper;
  private final DecisionClient decisionClient;

  private final PolicyRepository policyRepo;
  private final EventRecordRepository eventRepo;
  private final InventoryStateRepository inventoryRepo;
  private final ShipmentStateRepository shipmentRepo;
  private final DecisionRecordRepository decisionRepo;
  private final ActionRecordRepository actionRepo;
  private final DlqRecordRepository dlqRepo;

  public OrchestrationService(
      IdempotencyService idempotency,
      ObjectMapper mapper,
      DecisionClient decisionClient,
      PolicyRepository policyRepo,
      EventRecordRepository eventRepo,
      InventoryStateRepository inventoryRepo,
      ShipmentStateRepository shipmentRepo,
      DecisionRecordRepository decisionRepo,
      ActionRecordRepository actionRepo,
      DlqRecordRepository dlqRepo
  ) {
    this.idempotency = idempotency;
    this.mapper = mapper;
    this.decisionClient = decisionClient;
    this.policyRepo = policyRepo;
    this.eventRepo = eventRepo;
    this.inventoryRepo = inventoryRepo;
    this.shipmentRepo = shipmentRepo;
    this.decisionRepo = decisionRepo;
    this.actionRepo = actionRepo;
    this.dlqRepo = dlqRepo;
  }

  @Transactional
  public Map<String, Object> ingest(EventIngestRequest req) {
    // Idempotency: if already claimed, return quickly
    boolean claimed = idempotency.claimEvent(req.getEventId());
    if (!claimed) {
      return Map.of("status", "DUPLICATE", "eventId", req.getEventId());
    }

    OffsetDateTime eventTime = OffsetDateTime.parse(req.getEventTime());
    String correlationId = extractCorrelationId(req.getPayload());
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }

    // Persist raw event (audit)
    EventRecord er = new EventRecord();
    er.setId(UUID.randomUUID());
    er.setEventId(req.getEventId());
    er.setSource(req.getSource());
    er.setType(req.getType());
    er.setCorrelationId(correlationId);
    er.setEventTime(eventTime);
    er.setPayload(req.getPayload());
    er.setReceivedAt(OffsetDateTime.now(ZoneOffset.UTC));
    eventRepo.save(er);

    // Apply to operational truth store
    applyToState(req.getType(), req.getPayload(), eventTime);

    // Decide + execute for certain triggers
    if (List.of("InventoryLow", "ShipmentDelayed", "DemandSpike").contains(req.getType())) {
      runDecisionLoop(correlationId, req.getType());
    }

    return Map.of("status", "OK", "correlationId", correlationId);
  }

  private String extractCorrelationId(JsonNode payload) {
    if (payload != null && payload.hasNonNull("correlationId")) return payload.get("correlationId").asText();
    return null;
  }

  private void applyToState(String type, JsonNode payload, OffsetDateTime eventTime) {
    if (payload == null) return;

    switch (type) {
      case "InventoryLow" -> {
        String sku = payload.path("sku").asText("SKU-1");
        String loc = payload.path("location").asText("DC-SEA");
        int onHand = payload.path("onHand").asInt(10);
        int dailyDemand = payload.path("dailyDemand").asInt(25);

        InventoryState st = inventoryRepo.findBySkuAndLocation(sku, loc).orElseGet(() -> {
          InventoryState n = new InventoryState();
          n.setId(UUID.randomUUID());
          n.setSku(sku);
          n.setLocation(loc);
          return n;
        });
        st.setOnHand(onHand);
        st.setDailyDemand(dailyDemand);
        st.setUpdatedAt(eventTime);
        inventoryRepo.save(st);
      }
      case "DemandSpike" -> {
        String sku = payload.path("sku").asText("SKU-1");
        String loc = payload.path("location").asText("DC-SEA");
        int dailyDemand = payload.path("dailyDemand").asInt(35);

        InventoryState st = inventoryRepo.findBySkuAndLocation(sku, loc).orElseGet(() -> {
          InventoryState n = new InventoryState();
          n.setId(UUID.randomUUID());
          n.setSku(sku);
          n.setLocation(loc);
          n.setOnHand(20);
          return n;
        });
        st.setDailyDemand(dailyDemand);
        st.setUpdatedAt(eventTime);
        inventoryRepo.save(st);
      }
      case "ShipmentDelayed" -> {
        String shipmentId = payload.path("shipmentId").asText("SHP-1");
        int etaHours = payload.path("etaHours").asInt(36);
        String origin = payload.path("origin").asText("CN-SZX");
        String destination = payload.path("destination").asText("DC-SEA");
        String status = payload.path("status").asText("DELAYED");

        ShipmentState s = shipmentRepo.findByShipmentId(shipmentId).orElseGet(() -> {
          ShipmentState n = new ShipmentState();
          n.setId(UUID.randomUUID());
          n.setShipmentId(shipmentId);
          return n;
        });
        s.setEtaHours(etaHours);
        s.setOrigin(origin);
        s.setDestination(destination);
        s.setStatus(status);
        s.setUpdatedAt(eventTime);
        shipmentRepo.save(s);
      }
      default -> { /* ignore */ }
    }
  }

  private Policy getPolicy() {
    return policyRepo.findAll().stream().findFirst().orElseThrow();
  }

  @Transactional
  public void updatePolicy(Policy updated) {
    policyRepo.save(updated);
  }

  @Transactional
  public Map<String, Object> runDecisionLoop(String correlationId, String triggeredBy) {
    Policy policy = getPolicy();

    List<InventoryState> inv = inventoryRepo.findAll();
    List<ShipmentState> ships = shipmentRepo.findAll();

    Map<String, Object> policyMap = Map.of(
        "budget_cap_usd", policy.getBudgetCapUsd(),
        "approval_required_over_usd", policy.getApprovalRequiredOverUsd(),
        "max_expedites_per_day", policy.getMaxExpeditesPerDay(),
        "allowed_actions", parseAllowedActions(policy.getAllowedActionsJson()),
        "force_execution_failure", policy.isForceExecutionFailure()
    );

    List<Map<String, Object>> invList = inv.stream().map(i -> Map.<String, Object>of(
        "sku", i.getSku(),
        "location", i.getLocation(),
        "on_hand", i.getOnHand(),
        "daily_demand", i.getDailyDemand()
    )).toList();

    List<Map<String, Object>> shipList = ships.stream().map(s -> Map.<String, Object>of(
        "shipment_id", s.getShipmentId(),
        "status", s.getStatus(),
        "eta_hours", s.getEtaHours(),
        "origin", s.getOrigin(),
        "destination", s.getDestination()
    )).toList();

    Map<String, Object> req = new LinkedHashMap<>();
    req.put("correlation_id", correlationId);
    req.put("triggered_by", triggeredBy);
    req.put("policy", policyMap);
    req.put("inventory", invList);
    req.put("shipments", shipList);

    JsonNode decisionJson = decisionClient.decide(req);

    // persist decision record
    DecisionRecord dr = new DecisionRecord();
    dr.setId(UUID.randomUUID());
    dr.setCorrelationId(correlationId);
    dr.setTriggeredBy(triggeredBy);
    dr.setChosenAction(decisionJson.path("chosen").path("action").asText("REBALANCE"));
    dr.setExplanation(decisionJson.path("explanation"));
    dr.setDecidedAt(OffsetDateTime.now(ZoneOffset.UTC));
    decisionRepo.save(dr);

    // execute chosen action
    try {
      executeAction(policy, correlationId, dr.getChosenAction(), decisionJson);
      return Map.of("status", "EXECUTED", "correlationId", correlationId, "action", dr.getChosenAction());
    } catch (Exception ex) {
      pushToDlq(correlationId, ex.getMessage(), decisionJson);
      return Map.of("status", "DLQ", "correlationId", correlationId, "reason", ex.getMessage());
    }
  }

  private List<String> parseAllowedActions(String json) {
    try {
      return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
    } catch (Exception e) {
      return List.of("EXPEDITE","REBALANCE","REROUTE");
    }
  }

  private void executeAction(Policy policy, String correlationId, String actionType, JsonNode decisionJson) {
    if (policy.isForceExecutionFailure()) {
      throw new RuntimeException("Forced execution failure (demo toggle).");
    }

    ObjectNode details = mapper.createObjectNode();
    details.put("actionType", actionType);
    details.set("chosen", decisionJson.path("chosen"));
    details.put("note", "Simulated execution. Replace with real ERP/WMS/TMS connectors.");

    ActionRecord ar = new ActionRecord();
    ar.setId(UUID.randomUUID());
    ar.setCorrelationId(correlationId);
    ar.setActionType(actionType);
    ar.setStatus("SUCCEEDED");
    ar.setDetails(details);
    ar.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    actionRepo.save(ar);
  }

  private void pushToDlq(String correlationId, String reason, JsonNode decisionJson) {
    DlqRecord dlq = new DlqRecord();
    dlq.setId(UUID.randomUUID());
    dlq.setCorrelationId(correlationId);
    dlq.setReason(reason == null ? "Unknown error" : reason);
    dlq.setPayload(decisionJson);
    dlq.setStatus("PENDING");
    dlq.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    dlqRepo.save(dlq);
  }

  @Transactional
  public Map<String, Object> replayDlq(UUID dlqId) {
    DlqRecord rec = dlqRepo.findById(dlqId).orElseThrow();
    if (!"PENDING".equalsIgnoreCase(rec.getStatus())) {
      return Map.of("status", "SKIP", "message", "DLQ item not pending");
    }

    // Temporarily disable forced failure for replay to demonstrate recovery
    Policy p = getPolicy();
    boolean prior = p.isForceExecutionFailure();
    p.setForceExecutionFailure(false);
    policyRepo.save(p);

    try {
      String action = rec.getPayload().path("chosen").path("action").asText("REBALANCE");
      executeAction(p, rec.getCorrelationId(), action, rec.getPayload());
      rec.setStatus("REPLAYED");
      dlqRepo.save(rec);
      return Map.of("status", "REPLAYED", "dlqId", dlqId.toString());
    } finally {
      p.setForceExecutionFailure(prior);
      policyRepo.save(p);
    }
  }
}
