package com.ctk.orchestrator.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "policy")
public class Policy {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "budget_cap_usd", nullable = false)
  private int budgetCapUsd;

  @Column(name = "approval_required_over_usd", nullable = false)
  private int approvalRequiredOverUsd;

  @Column(name = "max_expedites_per_day", nullable = false)
  private int maxExpeditesPerDay;

  // stored as JSON string in DB to keep migration simple
  @Column(name = "allowed_actions", nullable = false, columnDefinition = "text")
  private String allowedActionsJson;

  @Column(name = "force_execution_failure", nullable = false)
  private boolean forceExecutionFailure;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public int getBudgetCapUsd() { return budgetCapUsd; }
  public void setBudgetCapUsd(int v) { this.budgetCapUsd = v; }

  public int getApprovalRequiredOverUsd() { return approvalRequiredOverUsd; }
  public void setApprovalRequiredOverUsd(int v) { this.approvalRequiredOverUsd = v; }

  public int getMaxExpeditesPerDay() { return maxExpeditesPerDay; }
  public void setMaxExpeditesPerDay(int v) { this.maxExpeditesPerDay = v; }

  public String getAllowedActionsJson() { return allowedActionsJson; }
  public void setAllowedActionsJson(String v) { this.allowedActionsJson = v; }

  public boolean isForceExecutionFailure() { return forceExecutionFailure; }
  public void setForceExecutionFailure(boolean v) { this.forceExecutionFailure = v; }

  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(OffsetDateTime t) { this.updatedAt = t; }
}
