package com.ctk.orchestrator.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EventIngestRequest {
  @NotBlank
  private String eventId;

  @NotBlank
  private String source; // ERP | WMS | TMS

  @NotBlank
  private String type; // DemandSpike | InventoryLow | ShipmentDelayed

  @NotBlank
  private String eventTime; // ISO-8601

  @NotNull
  private JsonNode payload;

  public String getEventId() { return eventId; }
  public void setEventId(String eventId) { this.eventId = eventId; }

  public String getSource() { return source; }
  public void setSource(String source) { this.source = source; }

  public String getType() { return type; }
  public void setType(String type) { this.type = type; }

  public String getEventTime() { return eventTime; }
  public void setEventTime(String eventTime) { this.eventTime = eventTime; }

  public JsonNode getPayload() { return payload; }
  public void setPayload(JsonNode payload) { this.payload = payload; }
}
