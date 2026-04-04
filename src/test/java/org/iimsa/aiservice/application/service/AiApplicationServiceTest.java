package org.iimsa.aiservice.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import org.iimsa.aiservice.application.dto.command.AiAnalysisRequestedCommand;
import org.iimsa.aiservice.domain.event.AiEvent;
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
        AiAnalysisRequestedCommand command = new AiAnalysisRequestedCommand(
                UUID.randomUUID(), UUID.randomUUID(), "SLACK_ID", "홍길동",
                List.of(new AiAnalysisRequestedCommand.Destination("서울역", 37.5, 126.9, "서울 중구"))
        );

        AiEntity mockAi = AiEntity.create(
                new Receiver(command.managerId(), command.managerSlackId(), command.receiverName()),
                "테스트 프롬프트"
        );

        // 행동 정의
        given(aiRepository.save(any())).willReturn(mockAi);
        given(aiAnalysisService.analyze(anyString())).willReturn("AI가 분석한 최적 경로 결과입니다.");

        // 2. When: 로직 실행
        aiApplicationService.handleAiAnalysisRequested(command);

        // 3. Then: 행위 검증
        verify(aiRepository, times(2)).save(any()); // 생성 시 1번, 완료 시 1번 총 2번 저장
        verify(aiAnalysisService).analyze(anyString()); // AI 분석 호출 확인
        verify(aiEvent).analysisCompleted(any()); // 외부 이벤트 발행 확인
    }
}
