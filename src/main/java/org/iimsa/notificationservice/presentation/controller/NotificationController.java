package org.iimsa.notificationservice.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.iimsa.config.security.UserDetailsImpl;
import org.iimsa.notificationservice.application.service.NotificationApplicationService;
import org.iimsa.notificationservice.domain.exception.NotificationAccessDeniedException;
import org.iimsa.notificationservice.presentation.dto.request.SendNotificationRequest;
import org.iimsa.notificationservice.presentation.dto.response.NotificationResponse;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Tag(name = "알림 API", description = "알림 발송 및 조회 API")
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;

    @Operation(
            summary = "알림 발송 요청 (테스트용)",
            description = "알림을 직접 발송합니다. 실제 운영에서는 Kafka Consumer가 자동으로 처리합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse sendNotification(
            @RequestBody SendNotificationRequest request) {
        return notificationApplicationService.sendNotification(request);
    }

    @Operation(
            summary = "알림 단건 조회",
            description = "MASTER는 전체 조회 가능, HUB_MANAGER는 담당 허브 소속 알림만 조회 가능합니다."
    )
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    @GetMapping("/{notificationId}")
    public NotificationResponse getNotification(
            @Parameter(description = "조회할 알림 ID", required = true)
            @PathVariable UUID notificationId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return notificationApplicationService.getNotification(notificationId);
    }

    @Operation(
            summary = "사용자별 알림 목록 조회",
            description = "MASTER는 전체 조회, HUB_MANAGER는 담당 허브 소속만, 배송기사는 본인 것만 조회 가능합니다."
    )
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'HUB_DELIVERY', 'COMPANY_DELIVERY')")
    @GetMapping("/users/{userId}")
    @PageableAsQueryParam
    public Page<NotificationResponse> getNotificationsByUser(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(hidden = true) @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // 배송기사는 본인 것만
        String roles = userDetails.getRoles();
        if ((roles.contains("HUB_DELIVERY") || roles.contains("COMPANY_DELIVERY"))
                && !userId.equals(userDetails.getUuid())) {
            throw new NotificationAccessDeniedException();
        }
        return notificationApplicationService.getNotificationsByUser(userId, pageable);
    }

    @Operation(
            summary = "전체 알림 목록 조회",
            description = "MASTER 전용 전체 조회입니다."
    )
    @PreAuthorize("hasRole('MASTER')")
    @GetMapping
    @PageableAsQueryParam
    public Page<NotificationResponse> getAllNotifications(
            @Parameter(hidden = true) @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return notificationApplicationService.getAllNotifications(pageable);
    }
}
