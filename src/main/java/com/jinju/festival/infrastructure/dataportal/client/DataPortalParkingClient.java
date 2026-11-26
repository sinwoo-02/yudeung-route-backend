package com.jinju.festival.infrastructure.dataportal.client;

import com.jinju.festival.infrastructure.dataportal.dto.DataPortalParkingItem;
import com.jinju.festival.infrastructure.dataportal.dto.DataPortalParkingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataPortalParkingClient {

    private final RestClient dataPortalRestClient;

    @Value("${api.data-portal.service-key}")
    private String serviceKey;

    @Value("${api.data-portal.base-url:https://apis.data.go.kr}")
    private String baseUrl;

    @Value("${api.data-portal.path:/5310000/jinjuparking/now}")
    private String apiPath;

    /**
     * 공공데이터포털(진주시 주차장) 실시간 정보 API 호출
     */
    public List<DataPortalParkingItem> fetchRealtimeParkingStatus() {
        if ("sample_service_key".equals(serviceKey) || serviceKey == null || serviceKey.isBlank()) {
            log.info("Default/Sample service key detected. Generating simulated live parking data for Jinju Festival.");
            return createSimulatedParkingItems();
        }

        try {
            String rawKey = serviceKey.trim();
            // 공공데이터포털 Decoding/Encoding 인증키 안전 인코딩 처리
            String encodedKey = rawKey.contains("%") ? rawKey : URLEncoder.encode(rawKey, StandardCharsets.UTF_8);
            String fullUrl = baseUrl + apiPath + "?serviceKey=" + encodedKey + "&numOfRows=100&pageNo=1";

            URI uri = URI.create(fullUrl);
            log.info("Requesting live parking data from Jinju Data Portal API: {}{}", baseUrl, apiPath);

            DataPortalParkingResponse response = dataPortalRestClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(DataPortalParkingResponse.class);

            if (response != null && !response.getItems().isEmpty()) {
                log.info("Successfully fetched {} live parking items from Jinju Data Portal API.", response.getItems().size());
                return response.getItems();
            } else {
                log.warn("Received empty items list from Jinju Data Portal API. Response header: {}",
                        response != null && response.getHeader() != null ? response.getHeader().getResultMsg() : "null");
            }
        } catch (Exception e) {
            String safeMessage = sanitizeMessage(e.getMessage());
            log.warn("Failed to fetch data from Data Portal API: {}. Falling back to simulation data.", safeMessage);
        }

        return createSimulatedParkingItems();
    }

    private String sanitizeMessage(String message) {
        if (message == null) {
            return "unknown error";
        }
        return message.replaceAll("(serviceKey=)[^&\\s]+", "$1***");
    }

    private List<DataPortalParkingItem> createSimulatedParkingItems() {
        // 실제 API 연동 불가 환경(로컬 개발/시연)을 위한 시뮬레이션 데이터 제공
        return List.of(
                DataPortalParkingItem.builder()
                        .prkplceNo("PARK_01")
                        .prkplceNm("진주종합경기장 임시주차장")
                        .rdnmadr("경상남도 진주시 동진로 415")
                        .totalPrkcm(1200)
                        .prkcm(720)
                        .disPrkcm(15)
                        .build(),
                DataPortalParkingItem.builder()
                        .prkplceNo("PARK_02")
                        .prkplceNm("신안강변도로 임시주차장")
                        .rdnmadr("경상남도 진주시 신안동 637-1")
                        .totalPrkcm(450)
                        .prkcm(440)
                        .disPrkcm(15)
                        .build(),
                DataPortalParkingItem.builder()
                        .prkplceNo("PARK_03")
                        .prkplceNm("진주성 공영주차장")
                        .rdnmadr("경상남도 진주시 남강로 626")
                        .totalPrkcm(180)
                        .prkcm(180)
                        .disPrkcm(6)
                        .build(),
                DataPortalParkingItem.builder()
                        .prkplceNo("PARK_04")
                        .prkplceNm("망경동 남강둔치 주차장")
                        .rdnmadr("경상남도 진주시 망경동 19-1")
                        .totalPrkcm(300)
                        .prkcm(145)
                        .disPrkcm(3)
                        .build()
        );
    }
}
