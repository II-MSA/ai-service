package org.iimsa.notificationservice.infrastructure.slack;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class SlackClient {

    private final WebClient webClient;

    @Value("${slack.token}")
    private String token;

    public SlackClient(WebClient.Builder webClientBuilder, @Value("${slack.token}") String token) {
        this.token = token;
        this.webClient = webClientBuilder
                .baseUrl("https://slack.com/api")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void sendMessage(String slackId, String message) {
        try {
            log.debug("[SlackClient] 메시지 전송 시도: 대상={}", slackId);

            ResponseEntity<JsonNode> res = webClient.post()
                    .uri("/chat.postMessage")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .bodyValue(Map.of("channel", slackId, "text", message))
                    .retrieve()
                    .toEntity(JsonNode.class)
                    .block(Duration.ofSeconds(10));

            if (res != null && res.getStatusCode().is2xxSuccessful()) {
                JsonNode body = res.getBody();
                if (body != null && body.get("ok").asBoolean()) {
                    log.info("[SlackClient] 전송 성공: slackId={}, ts={}", slackId, body.get("ts").asText());
                } else {
                    String error = (body != null && body.has("error")) ? body.get("error").asText() : "unknown_error";
                    log.error("[SlackClient] 슬랙 API 오류: {}", error);
                    throw new RuntimeException("Slack API 오류: " + error);
                }
            }
        } catch (Exception e) {
            log.error("[SlackClient] 전송 실패: {}", e.getMessage());
            throw new RuntimeException("슬랙 메시지 전송 실패: " + e.getMessage());
        }
    }
}
