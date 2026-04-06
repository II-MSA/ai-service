package org.iimsa.aiservice.domain.event;

import java.util.List;

public record AnalysisResponse(
        List<String> routeOrder, // ["남산타워", "홍대입구", "강남역"]
        String detailedReason    // "남산타워는 중심부라 적합하고..."
) {
}
