package com.ctk.orchestrator.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipment_state")
public class ShipmentState {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "shipment_id", nullable = false, unique = true)
  private String shipmentId;

  @Column(nullable = false)
  private String status;

  @Column(name = "eta_hours", nullable = false)
  private int etaHours;

  @Column(nullable = false)
  private String origin;

  @Column(nullable = false)
  private String destination;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public String getShipmentId() { return shipmentId; }
  public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public int getEtaHours() { return etaHours; }
  public void setEtaHours(int etaHours) { this.etaHours = etaHours; }

  public String getOrigin() { return origin; }
  public void setOrigin(String origin) { this.origin = origin; }

  public String getDestination() { return destination; }
  public void setDestination(String destination) { this.destination = destination; }

  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
