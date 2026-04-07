package org.iimsa.aiservice.application.service;

import java.util.List;
import java.util.UUID;
import org.iimsa.aiservice.domain.payload.AiAnalysisRequestedPayload;
import org.iimsa.aiservice.domain.payload.AiAnalysisRequestedPayload.Destination;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AiTest {

    @Autowired
    AiApplicationService service;

    @Test
    void test() {

        List<Destination> items = List.of(
                new Destination("목적지A", 37.5, 127.0, "서울특별시 송파구 송파대로 55"),
                new Destination("목적지B", 37.50001, 127.00002, "인천 남동구 정각로 29"),
                new Destination("목적지B", 37.50001, 127.00002, "세종특별자치시 한누리대로 2130")
        );

        AiAnalysisRequestedPayload payload = new AiAnalysisRequestedPayload(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "U0AQK4J3B9D",
                "manager",
                items
        );
        service.handleAiAnalysisRequested(payload);
    }
}
