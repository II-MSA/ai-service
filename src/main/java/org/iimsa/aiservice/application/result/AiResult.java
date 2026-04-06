package org.iimsa.aiservice.application.result;

import java.time.LocalDateTime;
import java.util.UUID;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.springframework.data.domain.Page;

/**
 * AI 분석 결과 응답 DTO
 */
public record AiResult(
        UUID id,
        UUID receiverId,
        String receiverName,
        String receiverSlackId,
        String prompt,
        String generatedText,
        String reason,
        LocalDateTime createdAt
) {

    public static AiResult from(AiEntity ai) {
        return new AiResult(
                ai.getId(),
                ai.getReceiver().getId(),
                ai.getReceiver().getReceiverName(),
                ai.getReceiver().getSlackId(),
                ai.getPrompt(),
                ai.getGeneratedText(),
                ai.getReason(),
                ai.getCreatedAt()
        );
    }

    public static Page<AiResult> fromPage(Page<AiEntity> page) {
        return page.map(AiResult::from);
    }
}