package com.ctk.orchestrator.repo;

import com.ctk.orchestrator.model.DlqRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DlqRecordRepository extends JpaRepository<DlqRecord, UUID> {
  List<DlqRecord> findTop100ByOrderByCreatedAtDesc();
}
