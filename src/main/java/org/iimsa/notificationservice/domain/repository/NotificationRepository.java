package org.iimsa.notificationservice.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.iimsa.notificationservice.domain.model.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepository {

    NotificationEntity save(NotificationEntity notification);

    Optional<NotificationEntity> findById(UUID notificationId);

    Page<NotificationEntity> findByReceiverId(UUID receiverId, Pageable pageable);

    Page<NotificationEntity> findAll(Pageable pageable);
}
