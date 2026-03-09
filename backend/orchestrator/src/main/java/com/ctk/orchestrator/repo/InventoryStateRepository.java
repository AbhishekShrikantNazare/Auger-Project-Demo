package com.ctk.orchestrator.repo;

import com.ctk.orchestrator.model.InventoryState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface InventoryStateRepository extends JpaRepository<InventoryState, UUID> {
  Optional<InventoryState> findBySkuAndLocation(String sku, String location);
}
