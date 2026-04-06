package org.iimsa.aiservice.infrastructure.persistence;

import java.util.UUID;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAiRepository extends JpaRepository<AiEntity, UUID> {
}