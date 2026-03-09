package com.ctk.orchestrator.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

public class TimelineItemDto {
  private String kind; // EVENT | DECISION | ACTION | DLQ
  private String correlationId;
  private String title;
  private String subtitle;
  private OffsetDateTime at;
  private JsonNode payload;

  public TimelineItemDto() {}

  public TimelineItemDto(String kind, String correlationId, String title, String subtitle, OffsetDateTime at, JsonNode payload) {
    this.kind = kind;
    this.correlationId = correlationId;
    this.title = title;
    this.subtitle = subtitle;
    this.at = at;
    this.payload = payload;
  }

  public String getKind() { return kind; }
  public void setKind(String kind) { this.kind = kind; }

  public String getCorrelationId() { return correlationId; }
  public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getSubtitle() { return subtitle; }
  public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

  public OffsetDateTime getAt() { return at; }
  public void setAt(OffsetDateTime at) { this.at = at; }

  public JsonNode getPayload() { return payload; }
  public void setPayload(JsonNode payload) { this.payload = payload; }
}
