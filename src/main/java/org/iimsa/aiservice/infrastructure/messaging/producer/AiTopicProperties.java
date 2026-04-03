package org.iimsa.aiservice.infrastructure.messaging.producer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 서비스 Kafka 토픽 설정 application.yml 의 kafka.topics 하위 값을 바인딩 kafka: topics: ai-analysis-done: ai.analysis.done.v1
 */
@Getter
@Setter
@Component //component를 안 붙인다면 @EnableConfigurationProperties(AiTopicProperties.class)로 스프링에게 스캔 시도
@ConfigurationProperties(prefix = "kafka.topics")
public class AiTopicProperties {

    private String aiAnalysisDone;
}

