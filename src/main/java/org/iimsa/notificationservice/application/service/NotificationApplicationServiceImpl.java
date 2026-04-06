package org.iimsa.notificationservice.application.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.notificationservice.domain.event.AiAnalysisCompletedPayload;
import org.iimsa.notificationservice.domain.exception.NotificationNotFoundException;
import org.iimsa.notificationservice.domain.model.NotificationEntity;
import org.iimsa.notificationservice.domain.repository.NotificationRepository;
import org.iimsa.notificationservice.infrastructure.slack.SlackClient;
import org.iimsa.notificationservice.presentation.dto.request.SendNotificationRequest;
import org.iimsa.notificationservice.presentation.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationApplicationServiceImpl implements NotificationApplicationService {

    private final NotificationRepository notificationRepository;
    private final SlackClient slackClient;

    @Override
    @Transactional
    public void sendNotification(AiAnalysisCompletedPayload payload) {
        log.info("[sendNotification] receiverId={}, slackId={}", payload.receiverId(), payload.receiverSlackId());

        NotificationEntity notification = NotificationEntity.create(
                payload.receiverId(),
                payload.receiverName(),
                payload.receiverSlackId(),
                payload.generatedText()
        );
        notificationRepository.save(notification);

        slackClient.sendMessage(payload.receiverSlackId(), payload.generatedText());

        log.info("[sendNotification] 알림 발송 완료. notificationId={}", notification.getId());
    }

    @Override
    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("[sendNotification] receiverId={}, slackId={}", request.receiverId(), request.receiverSlackId());

        NotificationEntity notification = NotificationEntity.create(
                request.receiverId(),
                request.receiverName(),
                request.receiverSlackId(),
                request.message()
        );
        notificationRepository.save(notification);

        slackClient.sendMessage(request.receiverSlackId(), request.message());

        log.info("[sendNotification] 알림 발송 완료. notificationId={}", notification.getId());
        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(UUID notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByUser(UUID userId, Pageable pageable) {
        return notificationRepository.findByReceiverId(userId, pageable)
                .map(NotificationResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(NotificationResponse::from);
    }
}
