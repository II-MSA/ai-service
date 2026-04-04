package org.iimsa.aiservice.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.iimsa.aiservice.domain.event.AnalysisResponse;
import org.iimsa.aiservice.domain.service.AiAnalysisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class AiApplicationServiceIT {

    @Autowired
    private AiAnalysisService aiAnalysisService;

    @Test
    @DisplayName("실제 OpenAI API 호출 결과 확인")
    void realOpenAiCallTest() {
        // given
        String prompt = """
                다음 배송 목적지들의 최적 경로를 분석하여 순서와 이유를 알려주세요.
                담당자: 이춘배
                배송 ID: test-001
                목적지 목록:
                  - 남산타워 (위도: 37.551000, 경도: 126.988000, 주소: 서울 용산구)
                  - 강남역 (위도: 37.498000, 경도: 127.027000, 주소: 서울 강남구)
                  - 홍대입구 (위도: 37.557000, 경도: 126.924000, 주소: 서울 마포구)
                최적 경로 순서와 각 선택 이유를 간결하게 작성해 주세요.
                """;

        // when
        AnalysisResponse result = aiAnalysisService.analyze(prompt);

        // then
        log.info("=== AI 분석 결과 ===");
        log.info("최적 경로 순서: {}", result.routeOrder());
        log.info("상세 이유: {}", result.detailedReason());
        log.info("==================");

        assertThat(result).isNotNull(); // 결과 객체 자체가 있어야 함
        assertThat(result.routeOrder()).isNotNull(); // 경로 리스트가 생성되어야 함
        assertThat(result.detailedReason()).isNotBlank(); // 상세 이유가 비어있지 않아야 함
    }
}
