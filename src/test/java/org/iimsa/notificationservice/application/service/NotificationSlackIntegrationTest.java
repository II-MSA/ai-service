package org.iimsa.notificationservice.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.UUID;
import org.iimsa.notificationservice.domain.event.AiAnalysisCompletedPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Notification 실제 Slack 전송 통합 테스트")
class NotificationSlackIntegrationTest {

    @Autowired
    private NotificationApplicationService notificationApplicationService;

    @Test
    @DisplayName("AI 분석 완료 알림 - 실제 Slack 전송")
    void sendNotification_실제슬랙전송() {
        // given
        AiAnalysisCompletedPayload payload = new AiAnalysisCompletedPayload(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "테스트 수신자",
                "U0AFCQSBUQY",
                "배송 최적 경로 분석이 완료되었습니다.\n최적 경로: A → B → C"
        );

        // when & then — 예외 없이 슬랙 전송 성공하면 통과
        assertDoesNotThrow(() -> notificationApplicationService.sendNotification(payload));
    }
}
