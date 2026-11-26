package com.jinju.festival.infrastructure.kakao.client;

import com.jinju.festival.infrastructure.kakao.dto.KakaoAddressResponse;
import com.jinju.festival.infrastructure.kakao.dto.KakaoDirectionsResponse;
import com.jinju.festival.infrastructure.kakao.dto.KakaoKeywordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoMapClient {

    private final RestClient kakaoLocalRestClient;
    private final RestClient kakaoNaviRestClient;

    @Value("${api.kakao.rest-api-key}")
    private String kakaoRestApiKey;

    /**
     * 주소 기반 좌표 검색 API (/v2/local/search/address.json)
     */
    public KakaoAddressResponse searchAddress(String address) {
        if ("sample_kakao_rest_api_key".equals(kakaoRestApiKey)) {
            log.info("Sample Kakao REST API key detected. Returning simulated address coordinate response.");
            return createSimulatedAddressResponse(address);
        }

        try {
            return kakaoLocalRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", address)
                            .build())
                    .retrieve()
                    .body(KakaoAddressResponse.class);
        } catch (Exception e) {
            log.warn("Failed to call Kakao Address API for '{}': {}. Falling back to simulation.", address, e.getMessage());
            return createSimulatedAddressResponse(address);
        }
    }

    /**
     * 키워드 장소 검색 API (/v2/local/search/keyword.json)
     */
    public KakaoKeywordResponse searchKeyword(String keyword) {
        if ("sample_kakao_rest_api_key".equals(kakaoRestApiKey)) {
            log.info("Sample Kakao REST API key detected. Returning simulated keyword search response.");
            return createSimulatedKeywordResponse(keyword);
        }

        try {
            return kakaoLocalRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/keyword.json")
                            .queryParam("query", keyword)
                            .build())
                    .retrieve()
                    .body(KakaoKeywordResponse.class);
        } catch (Exception e) {
            log.warn("Failed to call Kakao Keyword API for '{}': {}. Falling back to simulation.", keyword, e.getMessage());
            return createSimulatedKeywordResponse(keyword);
        }
    }

    /**
     * 카카오모빌리티 길찾기 API (/v1/directions)
     */
    public KakaoDirectionsResponse getDirections(Double originLon, Double originLat, Double destLon, Double destLat) {
        if ("sample_kakao_rest_api_key".equals(kakaoRestApiKey)) {
            log.info("Sample Kakao REST API key detected. Returning simulated directions response.");
            return createSimulatedDirectionsResponse(originLon, originLat, destLon, destLat);
        }

        try {
            String origin = originLon + "," + originLat;
            String destination = destLon + "," + destLat;

            return kakaoNaviRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/directions")
                            .queryParam("origin", origin)
                            .queryParam("destination", destination)
                            .queryParam("priority", "RECOMMEND")
                            .build())
                    .retrieve()
                    .body(KakaoDirectionsResponse.class);
        } catch (Exception e) {
            log.warn("Failed to call Kakao Directions API: {}. Falling back to simulation.", e.getMessage());
            return createSimulatedDirectionsResponse(originLon, originLat, destLon, destLat);
        }
    }

    private KakaoAddressResponse createSimulatedAddressResponse(String address) {
        return KakaoAddressResponse.builder()
                .documents(List.of(
                        KakaoAddressResponse.Document.builder()
                                .addressName(address)
                                .x("128.0841")
                                .y("35.1884")
                                .build()
                ))
                .build();
    }

    private KakaoKeywordResponse createSimulatedKeywordResponse(String keyword) {
        return KakaoKeywordResponse.builder()
                .documents(List.of(
                        KakaoKeywordResponse.Document.builder()
                                .placeName(keyword)
                                .addressName("경상남도 진주시 " + keyword)
                                .roadAddressName("경상남도 진주시 남강로 일원")
                                .x("128.0841")
                                .y("35.1884")
                                .build()
                ))
                .build();
    }

    private KakaoDirectionsResponse createSimulatedDirectionsResponse(Double originLon, Double originLat, Double destLon, Double destLat) {
        // 하버사인 직선 거리 기반 시뮬레이션 거리 계산 (m)
        double distance = calculateHaversineDistance(originLat, originLon, destLat, destLon);
        int duration = (int) (distance / 8.3); // 평균 시속 30km/h 가정

        return KakaoDirectionsResponse.builder()
                .routes(List.of(
                        KakaoDirectionsResponse.Route.builder()
                                .resultCode(0)
                                .resultMsg("success")
                                .summary(KakaoDirectionsResponse.Summary.builder()
                                        .distance((int) distance)
                                        .duration(duration)
                                        .build())
                                .sections(List.of(
                                        KakaoDirectionsResponse.Section.builder()
                                                .distance((int) distance)
                                                .duration(duration)
                                                .roads(List.of(
                                                        KakaoDirectionsResponse.Road.builder()
                                                                .name("남강로 구간")
                                                                .distance((int) distance)
                                                                .duration(duration)
                                                                .vertexes(List.of(originLon, originLat, destLon, destLat))
                                                                .build()
                                                ))
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private double calculateHaversineDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 500.0;
        final int R = 6371000; // 지구 반지름 (m)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
