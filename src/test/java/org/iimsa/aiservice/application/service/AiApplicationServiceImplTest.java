package org.iimsa.aiservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.iimsa.aiservice.application.dto.query.GetAiQuery;
import org.iimsa.aiservice.application.result.AiResult;
import org.iimsa.aiservice.domain.exception.AiNotFoundException;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.iimsa.aiservice.domain.model.Receiver;
import org.iimsa.aiservice.domain.repository.AiRepository;
import org.iimsa.common.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiApplicationServiceImpl 단위 테스트")
class AiApplicationServiceImplTest {

    @Mock
    private AiRepository aiRepository;

    @InjectMocks
    private AiApplicationServiceImpl aiApplicationService;

    private UUID aiId;
    private UUID receiverId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        aiId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);
    }

    // 테스트용 AiEntity Mock 생성 헬퍼
    private AiEntity createMockAiEntity() {
        AiEntity ai = mock(AiEntity.class);
        Receiver receiver = mock(Receiver.class);

        lenient().when(ai.getId()).thenReturn(aiId);
        lenient().when(ai.getReceiver()).thenReturn(receiver);
        lenient().when(ai.getPrompt()).thenReturn("배송 최적경로 분석 요청");
        lenient().when(ai.getGeneratedText()).thenReturn("최적 경로: A → B → C");
        lenient().when(ai.getReason()).thenReturn("거리 최소화 기준");
        lenient().when(ai.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 1, 1, 12, 0));

        lenient().when(receiver.getId()).thenReturn(receiverId);
        lenient().when(receiver.getReceiverName()).thenReturn("홍길동");
        lenient().when(receiver.getSlackId()).thenReturn("U123456");

        return ai;
    }

    // ─── getAi ───────────────────────────────────────────────

    @Nested
    @DisplayName("AI 분석 결과 단건 조회")
    class GetAi {

        @Test
        @DisplayName("성공 - 존재하는 ID면 AiResult 반환")
        void success() {
            // given
            AiEntity mockAi = createMockAiEntity();
            given(aiRepository.findById(aiId)).willReturn(Optional.of(mockAi));

            // when
            AiResult result = aiApplicationService.getAi(aiId);

            // then
            // getAi 성공 테스트 - id()로 수정
            assertThat(result.id()).isEqualTo(aiId);          // aiId() → id()
            assertThat(result.receiverId()).isEqualTo(receiverId);
            assertThat(result.generatedText()).isEqualTo("최적 경로: A → B → C");

            then(aiRepository).should(times(1)).findById(aiId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 ID면 AiNotFoundException 발생")
        void notFound() {
            // given
            given(aiRepository.findById(aiId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> aiApplicationService.getAi(aiId))
                    .isInstanceOf(AiNotFoundException.class);
        }
    }

    // ─── getAiListByReceiver ──────────────────────────────────

    @Nested
    @DisplayName("수신자별 AI 분석 결과 목록 조회")
    class GetAiListByReceiver {

        @Test
        @DisplayName("성공 - 결과가 있을 때 Page<AiResult> 반환")
        void success() {
            // given
            AiEntity mockAi = createMockAiEntity();
            Page<AiEntity> mockPage = new PageImpl<>(List.of(mockAi), pageable, 1);
            GetAiQuery query = GetAiQuery.ofByReceiver(receiverId, pageable);
            given(aiRepository.findByReceiverId(receiverId, pageable)).willReturn(mockPage);

            // when
            Page<AiResult> result = aiApplicationService.getAiListByReceiver(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).receiverId()).isEqualTo(receiverId);
            then(aiRepository).should(times(1)).findByReceiverId(receiverId, pageable);
        }

        @Test
        @DisplayName("성공 - 결과 없을 때 빈 Page 반환")
        void empty() {
            // given
            Page<AiEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            GetAiQuery query = GetAiQuery.ofByReceiver(receiverId, pageable);
            given(aiRepository.findByReceiverId(receiverId, pageable)).willReturn(emptyPage);

            // when
            Page<AiResult> result = aiApplicationService.getAiListByReceiver(query);

            // then
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ─── getAllAiList ─────────────────────────────────────────

    @Nested
    @DisplayName("전체 AI 분석 결과 목록 조회")
    class GetAllAiList {

        @Test
        @DisplayName("성공 - 전체 목록 Page<AiResult> 반환")
        void success() {
            // given
            AiEntity mockAi = createMockAiEntity();
            Page<AiEntity> mockPage = new PageImpl<>(List.of(mockAi), pageable, 1);
            GetAiQuery query = GetAiQuery.ofAll(pageable);
            given(aiRepository.findAll(pageable)).willReturn(mockPage);

            // when
            Page<AiResult> result = aiApplicationService.getAllAiList(query);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            then(aiRepository).should(times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("성공 - 데이터 없을 때 빈 Page 반환")
        void empty() {
            // given
            Page<AiEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            GetAiQuery query = GetAiQuery.ofAll(pageable);
            given(aiRepository.findAll(pageable)).willReturn(emptyPage);

            // when
            Page<AiResult> result = aiApplicationService.getAllAiList(query);

            // then
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ─── deleteAi ────────────────────────────────────────────

    @Nested
    @DisplayName("AI 분석 결과 삭제")
    class DeleteAi {

        @Test
        @DisplayName("성공 - 인증된 사용자명으로 softDelete 호출")
        void success() {
            // given
            AiEntity mockAi = createMockAiEntity();
            given(aiRepository.findById(aiId)).willReturn(Optional.of(mockAi));

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUsername)
                        .thenReturn(Optional.of("admin"));

                // when
                aiApplicationService.deleteAi(aiId);

                // then
                then(mockAi).should(times(1)).softDelete("admin");
                then(aiRepository).should(times(1)).save(mockAi);
            }
        }

        @Test
        @DisplayName("성공 - 인증 정보 없을 때 'system'으로 softDelete 호출")
        void withoutAuth() {
            // given
            AiEntity mockAi = createMockAiEntity();
            given(aiRepository.findById(aiId)).willReturn(Optional.of(mockAi));

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUsername)
                        .thenReturn(Optional.empty());

                // when
                aiApplicationService.deleteAi(aiId);

                // then
                then(mockAi).should(times(1)).softDelete("system");
                then(aiRepository).should(times(1)).save(mockAi);
            }
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 ID면 AiNotFoundException 발생, save 미호출")
        void notFound() {
            // given
            given(aiRepository.findById(aiId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> aiApplicationService.deleteAi(aiId))
                    .isInstanceOf(AiNotFoundException.class);
            then(aiRepository).should(never()).save(any());
        }
    }
}
