package org.iimsa.aiservice.domain.event;

//보낼 데이터
// UUID aiId // AI 분석 기록 ID UUID receiverId // 수신자 ID String receiverName // 수신자 이름 String receiverSlackId
// 수신자 Slack ID String generatedText // AI가 생성한 메시지 String reason // 분석 이유

import java.util.UUID;

/**
 * ai.analysis.done.v1 이벤트 페이로드
 */
public record AiAnalysisCompletedPayload(
        UUID aiId,
        UUID receiverId,
        String receiverName,
        String receiverSlackId,
        String generatedText,
        String reason
) {
}
