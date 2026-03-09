package com.ctk.orchestrator.api;

import com.ctk.orchestrator.api.dto.EventIngestRequest;
import com.ctk.orchestrator.api.dto.PolicyDto;
import com.ctk.orchestrator.api.dto.TimelineItemDto;
import com.ctk.orchestrator.model.Policy;
import com.ctk.orchestrator.repo.*;
import com.ctk.orchestrator.service.OrchestrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class ApiController {
  private final OrchestrationService orchestration;
  private final PolicyRepository policyRepo;
  private final EventRecordRepository eventRepo;
  private final DecisionRecordRepository decisionRepo;
  private final ActionRecordRepository actionRepo;
  private final DlqRecordRepository dlqRepo;
  private final ObjectMapper mapper;

  public ApiController(
      OrchestrationService orchestration,
      PolicyRepository policyRepo,
      EventRecordRepository eventRepo,
      DecisionRecordRepository decisionRepo,
      ActionRecordRepository actionRepo,
      DlqRecordRepository dlqRepo,
      ObjectMapper mapper
  ) {
    this.orchestration = orchestration;
    this.policyRepo = policyRepo;
    this.eventRepo = eventRepo;
    this.decisionRepo = decisionRepo;
    this.actionRepo = actionRepo;
    this.dlqRepo = dlqRepo;
    this.mapper = mapper;
  }

  @PostMapping("/events")
  public Map<String, Object> ingest(@Valid @RequestBody EventIngestRequest req) {
    return orchestration.ingest(req);
  }

  @GetMapping("/policy")
  public PolicyDto getPolicy() {
    Policy p = policyRepo.findAll().stream().findFirst().orElseThrow();
    PolicyDto dto = new PolicyDto();
    dto.setBudgetCapUsd(p.getBudgetCapUsd());
    dto.setApprovalRequiredOverUsd(p.getApprovalRequiredOverUsd());
    dto.setMaxExpeditesPerDay(p.getMaxExpeditesPerDay());
    try {
      dto.setAllowedActions(mapper.readValue(p.getAllowedActionsJson(),
          mapper.getTypeFactory().constructCollectionType(List.class, String.class)));
    } catch (Exception e) {
      dto.setAllowedActions(List.of("EXPEDITE","REBALANCE","REROUTE"));
    }
    dto.setForceExecutionFailure(p.isForceExecutionFailure());
    return dto;
  }

  @PutMapping("/policy")
  public Map<String, Object> updatePolicy(@Valid @RequestBody PolicyDto dto) throws Exception {
    Policy p = policyRepo.findAll().stream().findFirst().orElseThrow();
    p.setBudgetCapUsd(dto.getBudgetCapUsd());
    p.setApprovalRequiredOverUsd(dto.getApprovalRequiredOverUsd());
    p.setMaxExpeditesPerDay(dto.getMaxExpeditesPerDay());
    p.setAllowedActionsJson(mapper.writeValueAsString(dto.getAllowedActions()));
    p.setForceExecutionFailure(dto.isForceExecutionFailure());
    p.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    policyRepo.save(p);
    return Map.of("status", "OK");
  }

  @GetMapping("/timeline")
  public List<TimelineItemDto> timeline() {
    var events = eventRepo.findAll().stream()
        .sorted((a,b)->b.getReceivedAt().compareTo(a.getReceivedAt()))
        .limit(40)
        .map(e -> new TimelineItemDto(
            "EVENT",
            e.getCorrelationId(),
            e.getType(),
            e.getSource(),
            e.getReceivedAt(),
            e.getPayload()
        ));

    var decisions = decisionRepo.findTop50ByOrderByDecidedAtDesc().stream()
        .map(d -> new TimelineItemDto(
            "DECISION",
            d.getCorrelationId(),
            "Decision: " + d.getChosenAction(),
            "triggered by " + d.getTriggeredBy(),
            d.getDecidedAt(),
            d.getExplanation()
        ));

    var actions = actionRepo.findTop50ByOrderByCreatedAtDesc().stream()
        .map(a -> new TimelineItemDto(
            "ACTION",
            a.getCorrelationId(),
            "Execute: " + a.getActionType(),
            a.getStatus(),
            a.getCreatedAt(),
            a.getDetails()
        ));

    var dlq = dlqRepo.findTop100ByOrderByCreatedAtDesc().stream()
        .limit(20)
        .map(d -> new TimelineItemDto(
            "DLQ",
            d.getCorrelationId(),
            "DLQ: " + d.getReason(),
            d.getStatus(),
            d.getCreatedAt(),
            d.getPayload()
        ));

    return Stream.concat(Stream.concat(events, decisions), Stream.concat(actions, dlq))
        .sorted((a,b)->b.getAt().compareTo(a.getAt()))
        .limit(80)
        .toList();
  }

  @GetMapping("/dlq")
  public Object getDlq() {
    return dlqRepo.findTop100ByOrderByCreatedAtDesc();
  }

  @PostMapping("/replay/{dlqId}")
  public Map<String, Object> replay(@PathVariable("dlqId") UUID dlqId) {
    return orchestration.replayDlq(dlqId);
  }

  @PostMapping("/simulate/stockout")
  public Map<String, Object> simulateStockout() {
    String correlationId = UUID.randomUUID().toString();
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    EventIngestRequest e1 = new EventIngestRequest();
    e1.setEventId("evt-" + UUID.randomUUID());
    e1.setSource("TMS");
    e1.setType("ShipmentDelayed");
    e1.setEventTime(now.minusMinutes(1).toString());
    ObjectNode p1 = mapper.createObjectNode();
    p1.put("correlationId", correlationId);
    p1.put("shipmentId", "SHP-777");
    p1.put("status", "DELAYED");
    p1.put("etaHours", 36);
    p1.put("origin", "CN-SZX");
    p1.put("destination", "DC-SEA");
    e1.setPayload(p1);

    EventIngestRequest e2 = new EventIngestRequest();
    e2.setEventId("evt-" + UUID.randomUUID());
    e2.setSource("WMS");
    e2.setType("InventoryLow");
    e2.setEventTime(now.minusSeconds(30).toString());
    ObjectNode p2 = mapper.createObjectNode();
    p2.put("correlationId", correlationId);
    p2.put("sku", "SKU-1");
    p2.put("location", "DC-SEA");
    p2.put("onHand", 8);
    p2.put("dailyDemand", 25);
    e2.setPayload(p2);

    EventIngestRequest e3 = new EventIngestRequest();
    e3.setEventId("evt-" + UUID.randomUUID());
    e3.setSource("ERP");
    e3.setType("DemandSpike");
    e3.setEventTime(now.toString());
    ObjectNode p3 = mapper.createObjectNode();
    p3.put("correlationId", correlationId);
    p3.put("sku", "SKU-1");
    p3.put("location", "DC-SEA");
    p3.put("dailyDemand", 35);
    e3.setPayload(p3);

    orchestration.ingest(e1);
    orchestration.ingest(e2);
    var res = orchestration.ingest(e3);

    return Map.of(
        "status", "OK",
        "correlationId", correlationId,
        "last", res
    );
  }
}
