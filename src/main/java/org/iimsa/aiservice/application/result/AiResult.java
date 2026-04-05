package org.iimsa.aiservice.application.result;

import java.time.LocalDateTime;
import java.util.UUID;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.iimsa.aiservice.domain.model.Receiver;
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
        Receiver receiver = ai.getReceiver();
        return new AiResult(
                ai.getId(), receiver != null ? receiver.getId() : null,
                receiver != null ? receiver.getReceiverName() : null,
                receiver != null ? receiver.getSlackId() : null,
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
