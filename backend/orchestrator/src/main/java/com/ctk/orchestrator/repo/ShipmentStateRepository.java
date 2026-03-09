package com.ctk.orchestrator.repo;

import com.ctk.orchestrator.model.ShipmentState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentStateRepository extends JpaRepository<ShipmentState, UUID> {
  Optional<ShipmentState> findByShipmentId(String shipmentId);
}
