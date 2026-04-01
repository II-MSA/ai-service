package org.iimsa.aiservice.application.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.aiservice.application.dto.query.GetAiQuery;
import org.iimsa.aiservice.application.result.AiResult;
import org.iimsa.aiservice.domain.exception.AiNotFoundException;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.iimsa.aiservice.domain.repository.AiRepository;
import org.iimsa.common.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiApplicationServiceImpl implements AiApplicationService{

    private final AiRepository aiRepository;

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
}