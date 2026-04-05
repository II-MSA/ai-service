package org.iimsa.aiservice.application.service;

import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.aiservice.application.dto.query.GetAiQuery;
import org.iimsa.aiservice.application.result.AiResult;
import org.iimsa.aiservice.domain.event.AiAnalysisRequestedPayload;
import org.iimsa.aiservice.domain.event.AiEvent;
import org.iimsa.aiservice.domain.event.AnalysisResponse;
import org.iimsa.aiservice.domain.event.OrderConfirmedPayload;
import org.iimsa.aiservice.domain.exception.AiNotFoundException;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.iimsa.aiservice.domain.model.Receiver;
import org.iimsa.aiservice.domain.repository.AiRepository;
import org.iimsa.aiservice.domain.service.AiAnalysisService;
import org.iimsa.common.util.SecurityUtil;
import org.springframework.ai.converter.BeanOutputConverter;
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
     * ai.analysis.request.v1 처리   order->ai - 배송 최적 경로 분석을 수행하고 결과를 발행
     */
    @Override
    @Transactional
    public void handleAiAnalysisRequested(AiAnalysisRequestedPayload payload) {
        log.info("[handleAiAnalysisRequested] deliveryId={}, managerId={}, managerSlackId={}",
                payload.deliveryId(), payload.managerId(), payload.managerSlackId());

        String prompt = buildRouteOptimizationPrompt(payload);

        Receiver receiver = new Receiver(
                payload.managerId(),
                payload.managerSlackId(),
                payload.receiverName()
        );

        AiEntity ai = AiEntity.create(receiver, prompt);
        aiRepository.save(ai);

        AnalysisResponse response = aiAnalysisService.analyzeStructured(prompt);
        ai.complete(response.toString(), response.detailedReason());
        aiRepository.save(ai);

        ai.publishCompleted(aiEvent);

        log.info("[handleAiAnalysisRequested] AI 분석 완료. aiId={}", ai.getId());
    }

    @Override
    @Transactional
    public void handleOrderConfirmed(OrderConfirmedPayload payload) {
        log.info("[handleOrderConfirmed] orderId={}, hubId={}", payload.orderId(), payload.hubId());

        String prompt = buildOrderConfirmedPrompt(payload);

        Receiver receiver = new Receiver(
                payload.hubId(),
                payload.hubManagerSlackId(),
                payload.hubManagerName()
        );

        AiEntity ai = AiEntity.create(receiver, prompt);
        aiRepository.save(ai);

        String result = aiAnalysisService.analyze(prompt);
        ai.complete(result, "전달 이유"); //reason 고민 해봐야함.
        aiRepository.save(ai);
        ai.publishCompleted(aiEvent);

        log.info("[handleOrderConfirmed] AI 분석 완료. aiId={}", ai.getId());
    }

    private String buildRouteOptimizationPrompt(AiAnalysisRequestedPayload payload) {
        String destinationList = payload.destinations().stream()
                .map(d -> String.format("  - %s (위도: %.6f, 경도: %.6f, 주소: %s)",
                        d.name(), d.lat(), d.lng(), d.addr()))
                .collect(Collectors.joining("\n"));

        return String.format(
                "다음 배송 목적지들의 최적 경로를 분석하여 순서와 이유를 알려주세요.\n" +
                        "담당자: %s\n" +
                        "배송 ID: %s\n" +
                        "목적지 목록:\n%s\n" +
                        "최적 경로 순서와 각 선택 이유를 간결하게 작성해 주세요.",
                payload.receiverName(),
                payload.deliveryId(),
                destinationList
        );
        // analyze()에서 .entity를 사용했으므로 프롬포트 끝에 format을 직접 안넣어도 자동으로 붙는다.
    }

    private String buildOrderConfirmedPrompt(OrderConfirmedPayload payload) {
        var converter = new BeanOutputConverter<>(AnalysisResponse.class);
        String format = converter.getFormat();
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


}


