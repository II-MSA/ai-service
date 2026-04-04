package org.iimsa.aiservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.aiservice.application.dto.command.AiAnalysisRequestedCommand;
import org.iimsa.aiservice.application.dto.query.GetAiQuery;
import org.iimsa.aiservice.application.result.AiResult;
import org.iimsa.aiservice.domain.event.AiEvent;
import org.iimsa.aiservice.domain.exception.AiNotFoundException;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.iimsa.aiservice.domain.model.Receiver;
import org.iimsa.aiservice.domain.repository.AiRepository;
import org.iimsa.aiservice.domain.service.AiAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("AiApplicationServiceImpl 테스트")
class AiApplicationServiceImplTest {

    @Mock
    private AiRepository aiRepository;

    @InjectMocks
    private AiApplicationServiceImpl aiApplicationService;

    @Mock
    AiAnalysisService aiAnalysisService;
    @Mock
    AiEvent aiEvent;
    // ───────────────────────────────────────────────
    // 테스트 픽스처
    // ───────────────────────────────────────────────

    private UUID aiId;
    private UUID receiverId;
    private AiEntity aiEntity;

    @BeforeEach
    void setUp() {
        aiId = UUID.randomUUID();
        receiverId = UUID.randomUUID();

        Receiver receiver = new Receiver(receiverId, "U12345678", "홍길동");
        aiEntity = AiEntity.create(receiver, "배송 최적 경로 알려줘");
        aiEntity.complete("최적 경로는 A→B→C입니다.", "AI 자동 분석");

        log.info("[setUp] aiId={}, receiverId={}", aiId, receiverId);
    }

    @Test
    void handleAiAnalysisRequested_성공() {
        // given
        AiAnalysisRequestedCommand command = new AiAnalysisRequestedCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "U123SLACK",
                "홍길동",
                List.of(new AiAnalysisRequestedCommand.Destination("서울", 37.5, 127.0, "서울시 강남구"))
        );

        AiEntity mockAi = AiEntity.create(
                new Receiver(command.managerId(), command.managerSlackId(), command.receiverName()),
                "프롬프트"
        );
        given(aiRepository.save(any())).willReturn(mockAi);
        given(aiAnalysisService.analyze(any())).willReturn("최적 경로: 서울 → ...");

        // when
        aiApplicationService.handleAiAnalysisRequested(command);

        // then
        then(aiAnalysisService).should().analyze(any());
        then(aiEvent).should().analysisCompleted(any());
    }

    // ───────────────────────────────────────────────
    // getAi
    // ───────────────────────────────────────────────

    @Nested
    @DisplayName("단건 조회 - getAi()")
    class GetAiTest {

        @Test
        @DisplayName("존재하는 ID로 조회하면 AiResult를 반환한다")
        void getAi_success() {
            // given
            given(aiRepository.findById(aiId)).willReturn(Optional.of(aiEntity));
            log.info("[getAi_success] given - aiId={}", aiId);

            // when
            AiResult result = aiApplicationService.getAi(aiId);
            log.info("[getAi_success] when - result={}", result);

            // then
            assertThat(result).isNotNull();
            assertThat(result.receiverId()).isEqualTo(receiverId);
            assertThat(result.receiverName()).isEqualTo("홍길동");
            assertThat(result.receiverSlackId()).isEqualTo("U12345678");
            assertThat(result.generatedText()).isEqualTo("최적 경로는 A→B→C입니다.");
            assertThat(result.reason()).isEqualTo("AI 자동 분석");

            log.info("[getAi_success] then - receiverId={}, receiverName={}, generatedText={}",
                    result.receiverId(), result.receiverName(), result.generatedText());

            then(aiRepository).should(times(1)).findById(aiId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 AiNotFoundException이 발생한다")
        void getAi_notFound() {
            // given
            given(aiRepository.findById(aiId)).willReturn(Optional.empty());
            log.info("[getAi_notFound] given - 존재하지 않는 aiId={}", aiId);

            // when & then
            assertThatThrownBy(() -> aiApplicationService.getAi(aiId))
                    .isInstanceOf(AiNotFoundException.class);

            log.info("[getAi_notFound] then - AiNotFoundException 발생 확인 완료");
            then(aiRepository).should(times(1)).findById(aiId);
        }
    }

    // ───────────────────────────────────────────────
    // getAiListByReceiver
    // ───────────────────────────────────────────────

    @Nested
    @DisplayName("수신자별 목록 조회 - getAiListByReceiver()")
    class GetAiListByReceiverTest {

        @Test
        @DisplayName("수신자 ID로 조회하면 해당 수신자의 AI 결과 페이지를 반환한다")
        void getAiListByReceiver_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<AiEntity> entityPage = new PageImpl<>(List.of(aiEntity), pageable, 1);

            given(aiRepository.findByReceiverId(receiverId, pageable)).willReturn(entityPage);
            log.info("[getAiListByReceiver_success] given - receiverId={}, page={}, size={}",
                    receiverId, pageable.getPageNumber(), pageable.getPageSize());

            GetAiQuery query = GetAiQuery.ofByReceiver(receiverId, pageable);

            // when
            Page<AiResult> result = aiApplicationService.getAiListByReceiver(query);
            log.info("[getAiListByReceiver_success] when - totalElements={}", result.getTotalElements());

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().receiverId()).isEqualTo(receiverId);

            log.info("[getAiListByReceiver_success] then - 조회된 receiverId={}",
                    result.getContent().getFirst().receiverId());
            then(aiRepository).should(times(1)).findByReceiverId(receiverId, pageable);
        }

        @Test
        @DisplayName("수신자의 AI 결과가 없으면 빈 페이지를 반환한다")
        void getAiListByReceiver_empty() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<AiEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(aiRepository.findByReceiverId(receiverId, pageable)).willReturn(emptyPage);
            log.info("[getAiListByReceiver_empty] given - 결과 없는 receiverId={}", receiverId);

            GetAiQuery query = GetAiQuery.ofByReceiver(receiverId, pageable);

            // when
            Page<AiResult> result = aiApplicationService.getAiListByReceiver(query);
            log.info("[getAiListByReceiver_empty] when - totalElements={}", result.getTotalElements());

            // then
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
            log.info("[getAiListByReceiver_empty] then - 빈 페이지 반환 확인 완료");
        }
    }

    // ───────────────────────────────────────────────
    // getAllAiList
    // ───────────────────────────────────────────────

    @Nested
    @DisplayName("전체 목록 조회 - getAllAiList()")
    class GetAllAiListTest {

        @Test
        @DisplayName("전체 AI 결과 페이지를 반환한다")
        void getAllAiList_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<AiEntity> entityPage = new PageImpl<>(List.of(aiEntity), pageable, 1);

            given(aiRepository.findAll(pageable)).willReturn(entityPage);
            log.info("[getAllAiList_success] given - 전체 조회 page={}, size={}",
                    pageable.getPageNumber(), pageable.getPageSize());

            GetAiQuery query = GetAiQuery.ofAll(pageable);

            // when
            Page<AiResult> result = aiApplicationService.getAllAiList(query);
            log.info("[getAllAiList_success] when - totalElements={}", result.getTotalElements());

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            log.info("[getAllAiList_success] then - 전체 조회 완료");

            then(aiRepository).should(times(1)).findAll(pageable);
        }
    }

    // ───────────────────────────────────────────────
    // deleteAi
    // ───────────────────────────────────────────────

    @Nested
    @DisplayName("삭제 - deleteAi()")
    class DeleteAiTest {

        @Test
        @DisplayName("존재하는 AI 결과를 삭제하면 softDelete 후 save가 호출된다")
        void deleteAi_success() {
            // given
            given(aiRepository.findById(aiId)).willReturn(Optional.of(aiEntity));
            given(aiRepository.save(aiEntity)).willReturn(aiEntity);
            log.info("[deleteAi_success] given - 삭제할 aiId={}", aiId);

            // when
            aiApplicationService.deleteAi(aiId);
            log.info("[deleteAi_success] when - deleteAi 호출 완료");

            // then
            assertThat(aiEntity.getDeletedAt()).isNotNull();
            log.info("[deleteAi_success] then - deletedAt={} (softDelete 정상 작동)", aiEntity.getDeletedAt());

            then(aiRepository).should(times(1)).findById(aiId);
            then(aiRepository).should(times(1)).save(aiEntity);
        }

        @Test
        @DisplayName("존재하지 않는 AI 결과 삭제 시 AiNotFoundException이 발생한다")
        void deleteAi_notFound() {
            // given
            given(aiRepository.findById(aiId)).willReturn(Optional.empty());
            log.info("[deleteAi_notFound] given - 존재하지 않는 aiId={}", aiId);

            // when & then
            assertThatThrownBy(() -> aiApplicationService.deleteAi(aiId))
                    .isInstanceOf(AiNotFoundException.class);

            log.info("[deleteAi_notFound] then - AiNotFoundException 발생 확인, save 미호출 확인");
            then(aiRepository).should(times(1)).findById(aiId);
            then(aiRepository).should(times(0)).save(aiEntity);
        }


    }
}
