package org.iimsa.aiservice.presentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import org.iimsa.aiservice.application.result.AiResult;

/**
 * AI 분석 결과 응답 DTO (Presentation Layer)
 */
public record AiResponse(
        UUID id,
        UUID receiverId,
        String receiverName,
        String receiverSlackId,
        String prompt,
        String generatedText,
        String reason,
        LocalDateTime createdAt
) {
    public static AiResponse from(AiResult result) {
        return new AiResponse(
                result.id(),
                result.receiverId(),
                result.receiverName(),
                result.receiverSlackId(),
                result.prompt(),
                result.generatedText(),
                result.reason(),
                result.createdAt()
        );
    }
}

