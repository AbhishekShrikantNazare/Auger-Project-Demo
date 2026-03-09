package com.ctk.orchestrator.repo;

import com.ctk.orchestrator.model.EventRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EventRecordRepository extends JpaRepository<EventRecord, UUID> {
  Optional<EventRecord> findByEventId(String eventId);
}
