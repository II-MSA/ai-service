package org.iimsa.aiservice.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.iimsa.aiservice.application.dto.query.GetAiQuery;
import org.iimsa.aiservice.application.result.AiResult;
import org.iimsa.aiservice.application.service.AiApplicationService;
import org.iimsa.aiservice.presentation.dto.AiResponse;
import org.iimsa.common.response.CommonResponse;
import org.iimsa.common.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI", description = "AI 분석 결과 API")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiApplicationService aiApplicationService;

    @Operation(summary = "AI 분석 결과 단건 조회")
    @GetMapping("/{aiId}")
    public CommonResponse<AiResponse> getAi(@PathVariable UUID aiId) {
        AiResult result = aiApplicationService.getAi(aiId);
        return CommonResponse.success(AiResponse.from(result));
    }

    @Operation(summary = "내 AI 분석 결과 목록 조회")
    @GetMapping("/my")
    public CommonResponse<Page<AiResponse>> getMyAiList(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        UUID currentUserId = SecurityUtil.getCurrentUserIdOrThrow();
        GetAiQuery query = GetAiQuery.ofByReceiver(currentUserId, pageable);
        Page<AiResult> results = aiApplicationService.getAiListByReceiver(query);
        return CommonResponse.success(results.map(AiResponse::from));
    }

    @Operation(summary = "전체 AI 분석 결과 목록 조회 (MASTER 전용)")
    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public CommonResponse<Page<AiResponse>> getAllAiList(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        GetAiQuery query = GetAiQuery.ofAll(pageable);
        Page<AiResult> results = aiApplicationService.getAllAiList(query);
        return CommonResponse.success(results.map(AiResponse::from));
    }

    @Operation(summary = "AI 분석 결과 삭제 (MASTER 전용)")
    @DeleteMapping("/{aiId}")
    @PreAuthorize("hasRole('MASTER')")
    public CommonResponse<Void> deleteAi(@PathVariable UUID aiId) {
        aiApplicationService.deleteAi(aiId);
        return CommonResponse.success("AI 분석 결과가 삭제되었습니다.", null);
    }
}
