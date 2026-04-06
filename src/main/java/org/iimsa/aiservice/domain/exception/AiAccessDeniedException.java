package org.iimsa.aiservice.domain.exception;

import org.iimsa.common.exception.ForbiddenException;

public class AiAccessDeniedException extends ForbiddenException {
    public AiAccessDeniedException() {
        super("AI 분석 결과에 접근 권한이 없습니다.");
    }
}
