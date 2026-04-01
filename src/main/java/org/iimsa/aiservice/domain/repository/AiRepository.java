package org.iimsa.aiservice.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AiRepository {

    AiEntity save(AiEntity aiEntity);

    Optional<AiEntity> findById(UUID id);

    Page<AiEntity> findByReceiverId(UUID receiverId, Pageable pageable);

    Page<AiEntity> findAll(Pageable pageable);
}