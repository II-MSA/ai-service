package org.iimsa.aiservice.domain.exception;

import java.util.UUID;
import org.iimsa.common.exception.NotFoundException;

public class AiNotFoundException extends NotFoundException {

    public AiNotFoundException() {
        super("AI 분석 결과를 찾을 수 없습니다.");
    }

    public AiNotFoundException(UUID aiId) {
        super("AI 분석 결과를 찾을 수 없습니다. id=" + aiId);
    }

}