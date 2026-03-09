package com.ctk.orchestrator.repo;

import com.ctk.orchestrator.model.DecisionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DecisionRecordRepository extends JpaRepository<DecisionRecord, UUID> {
  List<DecisionRecord> findTop50ByOrderByDecidedAtDesc();
  List<DecisionRecord> findByCorrelationIdOrderByDecidedAtDesc(String correlationId);
}
