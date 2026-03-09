package com.ctk.orchestrator.repo;

import com.ctk.orchestrator.model.ActionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ActionRecordRepository extends JpaRepository<ActionRecord, UUID> {
  List<ActionRecord> findTop50ByOrderByCreatedAtDesc();
  List<ActionRecord> findByCorrelationIdOrderByCreatedAtDesc(String correlationId);
}
