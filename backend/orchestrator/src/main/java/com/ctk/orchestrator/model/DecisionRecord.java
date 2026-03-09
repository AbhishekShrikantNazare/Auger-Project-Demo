package com.ctk.orchestrator.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "decision_records")
public class DecisionRecord {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "correlation_id", nullable = false)
  private String correlationId;

  @Column(name = "triggered_by", nullable = false)
  private String triggeredBy;

  @Column(name = "chosen_action", nullable = false)
  private String chosenAction;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private JsonNode explanation;

  @Column(name = "decided_at", nullable = false)
  private OffsetDateTime decidedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public String getCorrelationId() { return correlationId; }
  public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

  public String getTriggeredBy() { return triggeredBy; }
  public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }

  public String getChosenAction() { return chosenAction; }
  public void setChosenAction(String chosenAction) { this.chosenAction = chosenAction; }

  public JsonNode getExplanation() { return explanation; }
  public void setExplanation(JsonNode explanation) { this.explanation = explanation; }

  public OffsetDateTime getDecidedAt() { return decidedAt; }
  public void setDecidedAt(OffsetDateTime decidedAt) { this.decidedAt = decidedAt; }
}
