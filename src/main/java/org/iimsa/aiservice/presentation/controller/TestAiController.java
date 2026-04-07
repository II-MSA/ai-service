package org.iimsa.aiservice.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.iimsa.aiservice.application.service.AiApplicationService;
import org.iimsa.aiservice.domain.payload.AiAnalysisRequestedPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestAiController {

    private final AiApplicationService aiApplicationService;

    @PostMapping("/analysis")
    public ResponseEntity<String> testAiAnalysis(@RequestBody AiAnalysisRequestedPayload payload) {
        aiApplicationService.handleAiAnalysisRequested(payload);

        return ResponseEntity.ok("AI 분석 로직 실행 완료! 인텔리제이 로그를 확인");
    }
}
