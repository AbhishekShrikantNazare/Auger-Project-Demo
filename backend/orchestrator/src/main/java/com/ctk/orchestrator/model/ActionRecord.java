package com.ctk.orchestrator.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "action_records")
public class ActionRecord {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "correlation_id", nullable = false)
  private String correlationId;

  @Column(name = "action_type", nullable = false)
  private String actionType;

  @Column(nullable = false)
  private String status;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private JsonNode details;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public String getCorrelationId() { return correlationId; }
  public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

  public String getActionType() { return actionType; }
  public void setActionType(String actionType) { this.actionType = actionType; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public JsonNode getDetails() { return details; }
  public void setDetails(JsonNode details) { this.details = details; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
