package org.iimsa.aiservice.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.iimsa.aiservice.application.dto.query.GetAiQuery;
import org.iimsa.aiservice.application.result.AiResult;
import org.iimsa.aiservice.application.service.AiApplicationService;
import org.iimsa.aiservice.domain.exception.AiAccessDeniedException;
import org.iimsa.aiservice.presentation.dto.AiResponse;
import org.iimsa.config.security.UserDetailsImpl;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI", description = "AI 분석 결과 API")
@RestController
@RequestMapping("/api/v1/ais")
@RequiredArgsConstructor
public class AiController {

    private final AiApplicationService aiApplicationService;

    @Operation(summary = "AI 분석 결과 단건 조회", description = "MASTER는 전체 조회 가능, HUB_MANAGER는 담당 허브 소속 분석 결과만 조회 가능합니다.")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    @GetMapping("/{aiId}")
    public AiResponse getAi(@Parameter(description = "조회할 AI 분석 결과 ID", required = true)
                            @PathVariable UUID aiId,
                            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        //String roles = userDetails.getRoles();
        //UUID currentUserId = userDetails.getUuid();
        AiResult result = aiApplicationService.getAi(aiId);
        return AiResponse.from(result);
    }

    @Operation(summary = "내 AI 분석 결과 목록 조회")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'HUB_DELIVERY_MANAGER')")
    @GetMapping("/users/{userId}")
    @PageableAsQueryParam
    public Page<AiResponse> getMyAiList(
            @Parameter(description = "조회할 사용자 ID", required = true) @PathVariable UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(hidden = true) Pageable pageable) {
        if (userDetails.getRoles().contains("HUB_DELIVERY_MANAGER")
                && !userId.equals(userDetails.getUuid())) {
            throw new AiAccessDeniedException();
        }
        GetAiQuery query = GetAiQuery.ofByReceiver(userId, pageable);
        return aiApplicationService.getAiListByReceiver(query).map(AiResponse::from);
    }

    @Operation(summary = "전체 AI 분석 결과 목록 조회 (MASTER 전용)")
    @PreAuthorize("hasRole('MASTER')")
    @GetMapping
    @PageableAsQueryParam
    public Page<AiResponse> getAllAiList(
            @Parameter(hidden = true) Pageable pageable) {
        GetAiQuery query = GetAiQuery.ofAll(pageable);
        return aiApplicationService.getAllAiList(query).map(AiResponse::from);
    }

    @Operation(summary = "AI 분석 결과 삭제 (MASTER 전용)")
    @PreAuthorize("hasRole('MASTER')")
    @DeleteMapping("/{aiId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAi(@PathVariable UUID aiId) {
        aiApplicationService.deleteAi(aiId);
    }
}
