package org.iimsa.notificationservice.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotificationAccessDeniedException extends RuntimeException {
    public NotificationAccessDeniedException() {
        super("알림에 접근 권한이 없습니다.");
    }
}
