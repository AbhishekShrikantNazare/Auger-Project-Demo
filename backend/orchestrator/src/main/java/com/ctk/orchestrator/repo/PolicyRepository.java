package com.ctk.orchestrator.repo;

import com.ctk.orchestrator.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PolicyRepository extends JpaRepository<Policy, UUID> {}
