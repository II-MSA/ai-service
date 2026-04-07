package org.iimsa.notificationservice.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import org.iimsa.notificationservice.domain.model.NotificationEntity;

public record NotificationResponse(
        UUID notificationId,
        UUID receiverId,
        String receiverName,
        String receiverSlackId,
        String message,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(NotificationEntity notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getReceiverId(),
                notification.getReceiverName(),
                notification.getReceiverSlackId(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}
