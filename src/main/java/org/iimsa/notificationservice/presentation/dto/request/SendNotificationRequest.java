package org.iimsa.notificationservice.presentation.dto.request;

import java.util.UUID;

public record SendNotificationRequest(
        UUID receiverId,
        String receiverName,
        String receiverSlackId,
        String message
) {
}
