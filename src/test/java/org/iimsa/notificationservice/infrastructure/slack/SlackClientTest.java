package org.iimsa.notificationservice.infrastructure.slack;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest(properties = {
        "slack.token=생략",
        "slack.url=http://localhost:8890"
})
class SlackClientTest {

    private MockWebServer mockWebServer;
    private SlackClient slackClient;
    private final String testToken = "test";

    @BeforeEach
    void setUp() throws IOException {
        // 1. 가짜 서버 시작
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // 2. 가짜 서버 주소로 WebClient 빌더 설정
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString());

        // 3. 테스트 대상 클래스 생성 (토큰 직접 주입)
        slackClient = new SlackClient(builder, testToken);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void 슬랙_메시지_전송_시_인증_토큰이_헤더에_포함되는지_확인() throws InterruptedException {
        // given: 가짜 서버가 응답할 내용 설정 (성공 응답)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"ok\": true, \"ts\": \"12345.6789\"}"));

        // when: 메시지 전송 실행
        slackClient.sendMessage("U0AFCQSBUQY", "테스트 메시지");

        // then: 가짜 서버가 받은 요청(Request)을 꺼내서 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        // Authorization 헤더가 내가 넣은 토큰으로 구성되었는지 확인
        assertThat(recordedRequest.getHeader("Authorization"))
                .isEqualTo("Bearer " + testToken);

        // 전송 대상(channel)과 내용(text)이 바디에 잘 담겼는지 확인
        assertThat(recordedRequest.getBody().readUtf8())
                .contains("U0AFCQSBUQY")
                .contains("테스트 메시지");
    }
}
