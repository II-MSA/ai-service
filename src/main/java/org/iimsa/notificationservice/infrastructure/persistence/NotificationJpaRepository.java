package org.iimsa.notificationservice.infrastructure.persistence;

import java.util.UUID;
import org.iimsa.notificationservice.domain.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {
}
