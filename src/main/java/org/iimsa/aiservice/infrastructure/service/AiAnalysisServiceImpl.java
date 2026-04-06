package org.iimsa.aiservice.infrastructure.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.aiservice.domain.event.AnalysisResponse;
import org.iimsa.aiservice.domain.service.AiAnalysisService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final ChatClient chatClient;

    @Override
    public AnalysisResponse analyzeStructured(String prompt) {
        AnalysisResponse result = chatClient.prompt()
                .user(prompt)
                .call()
                .entity(AnalysisResponse.class);

        log.info("[AiAnalysisServiceImpl] AI 분석 결과 result {}", result != null ? result : 0);

        return result;
    }

    @Override
    public String analyze(String prompt) {
        String result = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        log.info("[AiAnalysisServiceImpl] AI 분석 결과: {}", result);
        return result;
    }
}
