package org.iimsa.aiservice.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import org.iimsa.aiservice.domain.event.AiAnalysisRequestedPayload;
import org.iimsa.aiservice.domain.event.AiEvent;
import org.iimsa.aiservice.domain.event.AnalysisResponse;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.iimsa.aiservice.domain.model.Receiver;
import org.iimsa.aiservice.domain.repository.AiRepository;
import org.iimsa.aiservice.domain.service.AiAnalysisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiApplicationServiceTest {

    @InjectMocks
    private AiApplicationServiceImpl aiApplicationService;

    @Mock
    private AiRepository aiRepository;

    @Mock
    private AiAnalysisService aiAnalysisService;

    @Mock
    private AiEvent aiEvent;

    @Test
    @DisplayName("AI 분석 요청 시 생성, 분석, 완료 이벤트 발행이 순차적으로 일어난다")
    void handleAiAnalysisRequested_Success() {
        // 1. Given: 테스트 데이터 준비
        AiAnalysisRequestedPayload payload = new AiAnalysisRequestedPayload(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SLACK_ID",
                "홍길동",
                List.of(new AiAnalysisRequestedPayload.Destination("서울역", 37.5, 126.9, "서울 중구"))
        );

        // AI 응답 Mock 데이터 생성 (Record이므로 생성자로 간단히 생성)
        AnalysisResponse mockResponse = new AnalysisResponse(
                List.of("서울역"),
                "서울역은 중심부에 위치하여 첫 번째 목적지로 적합합니다."
        );

        AiEntity mockAi = AiEntity.create(
                new Receiver(payload.managerId(), payload.managerSlackId(), payload.receiverName()),
                "테스트 프롬프트"
        );

        // 행동 정의
        // aiRepository.save()가 호출될 때마다 mockAi를 리턴하도록 설정
        given(aiRepository.save(any())).willReturn(mockAi);

        // [핵심 수정] 이제 분석 서비스는 AnalysisResponse 객체를 리턴합니다.
        given(aiAnalysisService.analyzeStructured(anyString())).willReturn(mockResponse);

        // 2. When: 로직 실행
        aiApplicationService.handleAiAnalysisRequested(payload);

        // 3. Then: 행위 검증
        // 1) 생성 시 1번, AI 분석 완료 후 결과 업데이트 시 1번 -> 총 2번 저장을 검증합니다.
        verify(aiRepository, times(2)).save(any());

        // 2) AI 분석 서비스가 실제로 호출되었는지 확인합니다.
        verify(aiAnalysisService).analyze(anyString());

        // 3) 도메인 이벤트 발행 메서드가 호출되었는지 확인합니다. (ai.publishCompleted 내부 로직 확인)
        verify(aiEvent).analysisCompleted(any());
    }
}
