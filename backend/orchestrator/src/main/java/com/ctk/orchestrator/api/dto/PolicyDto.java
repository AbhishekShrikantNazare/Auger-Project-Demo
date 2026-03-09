package com.ctk.orchestrator.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PolicyDto {
  @NotNull
  private Integer budgetCapUsd;
  @NotNull
  private Integer approvalRequiredOverUsd;
  @NotNull
  private Integer maxExpeditesPerDay;

  @NotNull
  private List<String> allowedActions;

  private boolean forceExecutionFailure;

  public Integer getBudgetCapUsd() { return budgetCapUsd; }
  public void setBudgetCapUsd(Integer budgetCapUsd) { this.budgetCapUsd = budgetCapUsd; }

  public Integer getApprovalRequiredOverUsd() { return approvalRequiredOverUsd; }
  public void setApprovalRequiredOverUsd(Integer approvalRequiredOverUsd) { this.approvalRequiredOverUsd = approvalRequiredOverUsd; }

  public Integer getMaxExpeditesPerDay() { return maxExpeditesPerDay; }
  public void setMaxExpeditesPerDay(Integer maxExpeditesPerDay) { this.maxExpeditesPerDay = maxExpeditesPerDay; }

  public List<String> getAllowedActions() { return allowedActions; }
  public void setAllowedActions(List<String> allowedActions) { this.allowedActions = allowedActions; }

  public boolean isForceExecutionFailure() { return forceExecutionFailure; }
  public void setForceExecutionFailure(boolean forceExecutionFailure) { this.forceExecutionFailure = forceExecutionFailure; }
}
