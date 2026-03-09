package com.ctk.orchestrator.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_records")
public class EventRecord {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "event_id", nullable = false, unique = true)
  private String eventId;

  @Column(nullable = false)
  private String source;

  @Column(nullable = false)
  private String type;

  @Column(name = "correlation_id", nullable = false)
  private String correlationId;

  @Column(name = "event_time", nullable = false)
  private OffsetDateTime eventTime;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private JsonNode payload;

  @Column(name = "received_at", nullable = false)
  private OffsetDateTime receivedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public String getEventId() { return eventId; }
  public void setEventId(String eventId) { this.eventId = eventId; }

  public String getSource() { return source; }
  public void setSource(String source) { this.source = source; }

  public String getType() { return type; }
  public void setType(String type) { this.type = type; }

  public String getCorrelationId() { return correlationId; }
  public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

  public OffsetDateTime getEventTime() { return eventTime; }
  public void setEventTime(OffsetDateTime eventTime) { this.eventTime = eventTime; }

  public JsonNode getPayload() { return payload; }
  public void setPayload(JsonNode payload) { this.payload = payload; }

  public OffsetDateTime getReceivedAt() { return receivedAt; }
  public void setReceivedAt(OffsetDateTime receivedAt) { this.receivedAt = receivedAt; }
}
