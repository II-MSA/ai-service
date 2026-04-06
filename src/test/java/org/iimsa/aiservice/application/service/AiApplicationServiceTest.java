package org.iimsa.aiservice.application.service;

import static org.mockito.ArgumentMatchers.any;
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
import org.iimsa.aiservice.domain.payload.DeliveryAssignedPayload;
import org.iimsa.aiservice.domain.payload.OrderConfirmedPayload;
import org.iimsa.aiservice.domain.repository.AiRepository;
import org.iimsa.aiservice.domain.service.AiAnalysisService;
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
    private AiAnalysisService aiAnalysisService;
    @Mock
    private AiEvent aiEvent;

    @InjectMocks
    private AiApplicationServiceImpl aiApplicationService;

    // ─── handleAiAnalysisRequested ────────────────────────────

    @Nested
    @DisplayName("handleAiAnalysisRequested - 배송 최적 경로 분석")
    class HandleAiAnalysisRequested {

        @Test
        @DisplayName("성공 - 구조화 분석 후 결과 저장 및 이벤트 발행")
        void success() {
            // given
            AiAnalysisRequestedPayload payload = new AiAnalysisRequestedPayload(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "U_MANAGER_SLACK",
                    "홍길동",
                    List.of(
                            new AiAnalysisRequestedPayload.Destination("목적지A", 37.5, 127.0, "서울시 강남구"),
                            new AiAnalysisRequestedPayload.Destination("목적지B", 35.1, 129.0, "부산시 해운대구")
                    )
            );
            AnalysisResponse mockResponse = new AnalysisResponse(
                    List.of("목적지A", "목적지B"),
                    "거리 최소화 기준으로 정렬"
            );
            given(aiAnalysisService.analyzeStructured(anyString())).willReturn(mockResponse);

            // when
            aiApplicationService.handleAiAnalysisRequested(payload);

            // then
            then(aiAnalysisService).should(times(1)).analyzeStructured(anyString());
            then(aiRepository).should(times(2)).save(any(AiEntity.class));
            then(aiEvent).should(times(1)).analysisCompleted(any());
        }
    }

    // ─── handleOrderConfirmed ─────────────────────────────────

    @Nested
    @DisplayName("handleOrderConfirmed - 주문 확인 알림")
    class HandleOrderConfirmed {

        @Test
        @DisplayName("성공 - 허브매니저, 업체매니저 각각 분석 후 이벤트 2회 발행")
        void success() {
            //public record OrderConfirmedPayload(
            //        UUID orderId,
            //        String productName,
            //        int quantity,
            //        UUID hubId,
            //        String hubManagerSlackId,
            //        String hubManagerName,
            //        UUID companyManagerId,
            //        String companyManagerSlackId,
            //        String companyManagerName,
            //        String receiverCompanyName) {
            //}
            // given
            OrderConfirmedPayload payload = new OrderConfirmedPayload(
                    UUID.randomUUID(),   // orderId
                    "상품A",             // productName
                    10,                  // quantity
                    UUID.randomUUID(),   // hubId
                    "U_HUB_SLACK",      // hubManagerSlackId
                    "허브매니저",        // hubManagerName
                    UUID.randomUUID(),    // companyManagerId
                    "업체담당자",        // companyManagerName
                    "U_COMPANY_SLACK",  // companyManagerSlackId
                    "수령업체"          // receiverCompanyName
            );
            given(aiAnalysisService.analyze(anyString()))
                    .willReturn("허브 안내 메시지")
                    .willReturn("업체 안내 메시지");

            // when
            aiApplicationService.handleOrderConfirmed(payload);

            // then — 허브 + 업체 각각 save 2번 = 총 4번
            then(aiAnalysisService).should(times(2)).analyze(anyString());
            then(aiRepository).should(times(4)).save(any(AiEntity.class));
            then(aiEvent).should(times(2)).analysisCompleted(any());
        }
    }

    // ─── handleDeliveryAssigned ───────────────────────────────

    @Nested
    @DisplayName("handleDeliveryAssigned - 배송 기사 경로 안내")
    class HandleDeliveryAssigned {

        @Test
        @DisplayName("성공 - 경로 안내 분석 후 결과 저장 및 이벤트 발행")
        void success() {
            // given
            DeliveryAssignedPayload payload = new DeliveryAssignedPayload(
                    UUID.randomUUID(),      // deliveryId
                    UUID.randomUUID(),      // orderId
                    "상품A",               // productName
                    "수령업체",             // receiverCompanyName
                    UUID.randomUUID(),      // deliveryManagerId
                    "배송기사",             // deliveryManagerName
                    "U_DELIVERY_SLACK",    // deliveryManagerSlackId
                    "서울허브 → 대전허브 → 부산허브"  // routeSummary
            );
            given(aiAnalysisService.analyze(anyString())).willReturn("경로 안내 메시지");

            // when
            aiApplicationService.handleDeliveryAssigned(payload);

            // then
            then(aiAnalysisService).should(times(1)).analyze(anyString());
            then(aiRepository).should(times(2)).save(any(AiEntity.class));
            then(aiEvent).should(times(1)).analysisCompleted(any());
        }
    }
}
