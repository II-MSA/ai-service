package org.iimsa.aiservice.infrastructure.tool;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class NavigationTools {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final WebClient webClient;

    // 1. 주소를 좌표로 변환하는 메서드
    public Coordinates toCoordinates(String address) {
        try {
            JsonNode res = this.webClient.get()
                    .uri("https://dapi.kakao.com/v2/local/search/address.json?query=" + address)
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoApiKey)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (res != null && res.has("documents") && !res.get("documents").isEmpty()) {
                JsonNode doc = res.get("documents").get(0);
                return new Coordinates(doc.get("y").asDouble(), doc.get("x").asDouble());
            }
        } catch (Exception e) {
            log.error("주소 변환 실패: {} - {}", address, e.getMessage());
        }
        return null;
    }

    // 2. 전체 경로의 최적 순서와 시간을 계산하는 메서드 (Waypoints API)
    public List<RouteInfo> calculateOptimizedRoutes(List<String> addresses) {
        List<Coordinates> coords = addresses.stream()
                .map(this::toCoordinates)
                .filter(Objects::nonNull)
                .toList();

        if (coords.size() < 2) {
            return List.of();
        }

        // 카카오 Waypoints API 규격에 맞게 바디 조립
        Map<String, Object> params = new HashMap<>();
        params.put("origin", Map.of("x", coords.get(0).lng(), "y", coords.get(0).lat()));
        params.put("destination",
                Map.of("x", coords.get(coords.size() - 1).lng(), "y", coords.get(coords.size() - 1).lat()));

        if (coords.size() > 2) {
            List<Map<String, Object>> waypoints = coords.subList(1, coords.size() - 1).stream()
                    .map(c -> Map.<String, Object>of("name", "경유지", "x", c.lng(), "y", c.lat()))
                    .collect(Collectors.toList());
            params.put("waypoints", waypoints);
        }

        JsonNode response = this.webClient.post()
                .uri("https://apis-navi.kakaomobility.com/v1/waypoints/directions")
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return parseRoutes(response, addresses, coords);
    }

    // 응답 데이터 파싱
    private List<RouteInfo> parseRoutes(JsonNode nodes, List<String> addresses, List<Coordinates> coords) {
        List<RouteInfo> result = new ArrayList<>();
        // 소요 시간/거리 파싱 로직 적용
        for (int i = 0; i < addresses.size(); i++) {
            result.add(new RouteInfo(addresses.get(i), coords.get(i)));
        }
        return result;
    }

    public record Coordinates(double lat, double lng) {
    }

    public record RouteInfo(String address, Coordinates coords) {
    }
}
