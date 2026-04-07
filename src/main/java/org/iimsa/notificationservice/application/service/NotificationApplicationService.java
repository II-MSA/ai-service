package org.iimsa.notificationservice.application.service;

import java.util.UUID;
import org.iimsa.notificationservice.domain.event.AiAnalysisCompletedPayload;
import org.iimsa.notificationservice.presentation.dto.request.SendNotificationRequest;
import org.iimsa.notificationservice.presentation.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationApplicationService {

    /**
     * 알림 발송
     */
    void sendNotification(AiAnalysisCompletedPayload payload);

    NotificationResponse sendNotification(SendNotificationRequest request);

    /**
     * 알림 단건 조회
     */
    NotificationResponse getNotification(UUID notificationId);

    /**
     * 사용자별 알림 목록 조회
     */
    Page<NotificationResponse> getNotificationsByUser(UUID userId, Pageable pageable);

    /**
     * 전체 알림 목록 조회 (MASTER 전용)
     */
    Page<NotificationResponse> getAllNotifications(Pageable pageable);
}
