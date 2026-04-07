package org.iimsa.aiservice.application.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.aiservice.application.dto.query.GetAiQuery;
import org.iimsa.aiservice.application.result.AiResult;
import org.iimsa.aiservice.domain.event.AiEvent;
import org.iimsa.aiservice.domain.event.AnalysisResponse;
import org.iimsa.aiservice.domain.exception.AiNotFoundException;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.iimsa.aiservice.domain.model.Receiver;
import org.iimsa.aiservice.domain.payload.AiAnalysisRequestedPayload;
import org.iimsa.aiservice.domain.payload.DeliveryAssignedPayload;
import org.iimsa.aiservice.domain.payload.OrderConfirmedPayload;
import org.iimsa.aiservice.domain.repository.AiRepository;
import org.iimsa.aiservice.domain.service.AiAnalysisService;
import org.iimsa.aiservice.infrastructure.tool.NavigationTools;
import org.iimsa.common.util.SecurityUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiApplicationServiceImpl implements AiApplicationService {

    private final AiRepository aiRepository;
    private final AiAnalysisService aiAnalysisService;
    private final AiEvent aiEvent;
    private final NavigationTools navigationTools;
    private final ChatClient chatClient;

    @Override
    @Transactional(readOnly = true)
    public AiResult getAi(UUID aiId) {
        AiEntity ai = aiRepository.findById(aiId)
                .orElseThrow(() -> new AiNotFoundException(aiId));
        return AiResult.from(ai);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiResult> getAiListByReceiver(GetAiQuery query) {
        return AiResult.fromPage(
                aiRepository.findByReceiverId(query.receiverId(), query.pageable())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiResult> getAllAiList(GetAiQuery query) {
        return AiResult.fromPage(
                aiRepository.findAll(query.pageable())
        );
    }

    @Override
    @Transactional
    public void deleteAi(UUID aiId) {

        AiEntity ai = aiRepository.findById(aiId)
                .orElseThrow(() -> new AiNotFoundException(aiId));
        String deletedBy = SecurityUtil.getCurrentUsername().orElse("system");
        ai.softDelete(deletedBy);
        aiRepository.save(ai);
    }

    /**
     * ai.analysis.request.v1 처리   delivery->ai - 배송 최적 경로 분석을 수행하고 결과를 발행
     */
    @Override
    @Transactional
    public void handleAiAnalysisRequested(AiAnalysisRequestedPayload payload) {
        log.info("[handleAiAnalysisRequested] deliveryId={}, managerId={}, managerSlackId={}",
                payload.deliveryId(), payload.managerId(), payload.managerSlackId());

        //payload에서 Destination(tring name,double lat,double lng,String addr) 을 꺼낸다.
        List<String> addresses = payload.destinations().stream()
                .map(AiAnalysisRequestedPayload.Destination::addr)
                .toList();

        //전체 경로의 최적 순서와 시간을 계산하는 메서드를 호출
        List<NavigationTools.RouteInfo> realRouteData = navigationTools.calculateOptimizedRoutes(addresses);

        //페르소나 주입
        String prompt = buildProfessionalPrompt(payload, realRouteData);

        //기록 저장 (시작)
        Receiver receiver = new Receiver(payload.managerId(), payload.managerSlackId(), payload.receiverName());
        AiEntity ai = AiEntity.create(receiver, prompt);
        aiRepository.save(ai);
        //요청시간 주입

        // 4. AI 분석
        AnalysisResponse response = aiAnalysisService.analyzeStructured(prompt);

        // 5. 완료 처리
        //완료 시간 주입
        ai.complete(response.toString(), response.detailedReason());
        aiRepository.save(ai);

        ai.publishCompleted(aiEvent);
        log.info("[handleAiAnalysisRequested] AI 분석 완료! aiId={}", ai.getId());
    }

    //주문 완료시. order -> ai
    @Override
    @Transactional
    public void handleOrderConfirmed(OrderConfirmedPayload payload) {
        log.info("[handleOrderConfirmed] orderId={}, hubId={}", payload.orderId(), payload.hubId());

        // 1. 허브 매니저용
        String hubPrompt = buildHubManagerPrompt(payload);
        Receiver hubManagerReceiver = new Receiver(
                payload.hubId(),
                payload.hubManagerSlackId(),
                payload.hubManagerName()
        );
        AiEntity hubAi = AiEntity.create(hubManagerReceiver, hubPrompt);
        aiRepository.save(hubAi);
        String hubResult = aiAnalysisService.analyze(hubPrompt);
        hubAi.complete(hubResult, null);
        aiRepository.save(hubAi);
        hubAi.publishCompleted(aiEvent);

        // 2. 업체 매니저용
        String companyPrompt = buildCompanyManagerPrompt(payload);
        Receiver companyManagerReceiver = new Receiver(
                payload.companyManagerId(),
                payload.companyManagerSlackId(),
                payload.companyManagerName()
        );
        AiEntity companyAi = AiEntity.create(companyManagerReceiver, companyPrompt);
        aiRepository.save(companyAi);
        String companyResult = aiAnalysisService.analyze(companyPrompt);
        companyAi.complete(companyResult, null);
        aiRepository.save(companyAi);
        companyAi.publishCompleted(aiEvent);

        log.info("[handleOrderConfirmed] AI 분석 완료. hubAiId={}, companyAiId={}", hubAi.getId(), companyAi.getId());
    }

    /**
     * delivery.assigned.v1 처리 - 배달 담당자 배정 시 경로 요약을 AI로 안내합니다.
     */
    @Override
    @Transactional
    public void handleDeliveryAssigned(DeliveryAssignedPayload payload) {
        log.info("[handleDeliveryAssigned] deliveryId={}, deliveryManagerId={}",
                payload.deliveryId(), payload.deliveryManagerId());

        String prompt = buildDeliveryAssignedPrompt(payload);

        Receiver receiver = new Receiver(
                payload.deliveryManagerId(),
                payload.deliveryManagerSlackId(),
                payload.deliveryManagerName()
        );

        AiEntity ai = AiEntity.create(receiver, prompt);
        aiRepository.save(ai);

        String result = aiAnalysisService.analyze(prompt);
        ai.complete(result, null);
        aiRepository.save(ai);
        ai.publishCompleted(aiEvent);

        log.info("[handleDeliveryAssigned] AI 분석 완료. aiId={}", ai.getId());
    }

    private String buildHubManagerPrompt(OrderConfirmedPayload payload) {
        return String.format(
                "주문이 확인되었습니다. 허브 매니저(%s)에게 아래 정보를 안내해 주세요.\n" +
                        "- 상품: %s\n" +
                        "- 수량: %d\n" +
                        "- 수령 업체: %s\n" +
                        "- 업체 담당자: %s\n" +
                        "친절한 말투로 배송 안내 메시지를 작성해 주세요.",
                payload.hubManagerName(),
                payload.productName(),
                payload.quantity(),
                payload.receiverCompanyName(),
                payload.companyManagerName()
        );
    }

    private String buildCompanyManagerPrompt(OrderConfirmedPayload payload) {
        return String.format(
                "주문이 확인되었습니다. 업체 담당자(%s)에게 아래 정보를 안내해 주세요.\n" +
                        "- 상품: %s\n" +
                        "- 수량: %d\n" +
                        "- 수령 업체: %s\n" +
                        "친절한 말투로 배송 안내 메시지를 작성해 주세요.",
                payload.companyManagerName(),
                payload.productName(),
                payload.quantity(),
                payload.receiverCompanyName()
        );
    }

    private String buildProfessionalPrompt(AiAnalysisRequestedPayload payload, List<NavigationTools.RouteInfo> routes) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "지금부터 당신의 역할은 10년 경력의 베테랑 배송 마스터입니다. 아래 카카오 내비게이션 엔진이 계산한 실시간 최적 경로 데이터를 바탕으로 배송 기사님께 전문적인 조언을 해주세요.\n\n");
        sb.append("### 실시간 경로 데이터 ###\n");
        for (int i = 0; i < routes.size(); i++) {
            sb.append(String.format("%d. %s (좌표: %.6f, %.6f)\n",
                    i + 1, routes.get(i).address(), routes.get(i).coords().lat(), routes.get(i).coords().lng()));
        }
        sb.append("\n### 요구사항 ###\n");
        sb.append("1. 위 순서대로 배송했을 때의 장점을 베테랑의 시선에서 설명해주세요.\n");
        sb.append("2. 기사님(%s님)을 향한 짧은 응원의 메시지를 포함해주세요.\n");
        sb.append("3. 답변은 존댓말로, 친절하고 전문적인 말투여야 합니다.");

        return sb.toString();
    }

    //delivery.assigned.v1 처리
    private String buildDeliveryAssignedPrompt(DeliveryAssignedPayload payload) {
        return String.format(
                "배송이 배정되었습니다. 배송기사(%s)에게 아래 배송 경로를 안내해 주세요.\n" +
                        "- 상품: %s\n" +
                        "- 수령 업체: %s\n" +
                        "- 배송 경로: %s\n" +
                        "친절한 말투로 배송 경로 안내 메시지를 작성해 주세요.",
                payload.deliveryManagerName(),
                payload.productName(),
                payload.receiverCompanyName(),
                payload.routeSummary()
        );
    }
