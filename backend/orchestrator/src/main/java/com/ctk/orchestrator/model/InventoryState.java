package com.ctk.orchestrator.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_state", uniqueConstraints = @UniqueConstraint(columnNames = {"sku","location"}))
public class InventoryState {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false)
  private String sku;

  @Column(nullable = false)
  private String location;

  @Column(name = "on_hand", nullable = false)
  private int onHand;

  @Column(name = "daily_demand", nullable = false)
  private int dailyDemand;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public String getSku() { return sku; }
  public void setSku(String sku) { this.sku = sku; }

  public String getLocation() { return location; }
  public void setLocation(String location) { this.location = location; }

  public int getOnHand() { return onHand; }
  public void setOnHand(int onHand) { this.onHand = onHand; }

  public int getDailyDemand() { return dailyDemand; }
  public void setDailyDemand(int dailyDemand) { this.dailyDemand = dailyDemand; }

  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
