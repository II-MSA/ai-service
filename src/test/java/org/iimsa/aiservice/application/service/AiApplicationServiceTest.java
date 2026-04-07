package org.iimsa.aiservice.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.UUID;
import org.iimsa.aiservice.domain.event.AiEvent;
import org.iimsa.aiservice.domain.event.AnalysisResponse;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.iimsa.aiservice.domain.payload.AiAnalysisRequestedPayload;
import org.iimsa.aiservice.domain.repository.AiRepository;
import org.iimsa.aiservice.domain.service.AiAnalysisService;
import org.iimsa.aiservice.infrastructure.tool.NavigationTools;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiApplicationServiceImpl 이벤트 핸들러 테스트")
class AiApplicationServiceImplEventTest {

    @Mock
    private AiRepository aiRepository;
    @Mock
    private AiAnalysisService aiAnalysisService; // 👈 우리 리모컨!
    @Mock
    private AiEvent aiEvent;
    @Mock
    private NavigationTools navigationTools;

    @InjectMocks
    private AiApplicationServiceImpl aiApplicationService;

    @Nested
    @DisplayName("handleAiAnalysisRequested - 배송 최적 경로 분석")
    class HandleAiAnalysisRequested {

        @Test
        @DisplayName("성공 - 분석 서비스(리모컨)를 통해 결과를 받아오고 저장한다")
        void success() {
            // given
            AiAnalysisRequestedPayload payload = createTestPayload();

            // 카카오 도구 가짜 응답
            given(navigationTools.calculateOptimizedRoutes(anyList()))
                    .willReturn(List.of(new NavigationTools.RouteInfo("서울", new NavigationTools.Coordinates(37, 127))));

            // ✨ 리모컨(aiAnalysisService) 가짜 응답 ✨
            AnalysisResponse mockResponse = new AnalysisResponse(List.of("목적지A"), "분석 완료");
            given(aiAnalysisService.analyzeStructured(anyString())).willReturn(mockResponse);

            // when
            aiApplicationService.handleAiAnalysisRequested(payload);

            // then
            // 1. 리모컨이 한 번 불렸는지 확인
            then(aiAnalysisService).should(times(1)).analyzeStructured(anyString());
            // 2. 저장이 두 번(생성 시, 완료 시) 됐는지 확인
            then(aiRepository).should(times(2)).save(any(AiEntity.class));
            // 3. 완료 이벤트가 나갔는지 확인
            then(aiEvent).should(times(1)).analysisCompleted(any());
        }
    }

    // 테스트용 페이로드 생성 편의 메서드
    private AiAnalysisRequestedPayload createTestPayload() {
        return new AiAnalysisRequestedPayload(
                UUID.randomUUID(), UUID.randomUUID(), "SLACK_ID", "가은기사님",
                List.of(new AiAnalysisRequestedPayload.Destination("목적지A", 37.5, 127.0, "서울"))
        );
    }
}
