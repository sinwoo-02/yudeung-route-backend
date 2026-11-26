package com.jinju.festival.infrastructure.kakao.service;

import com.jinju.festival.infrastructure.kakao.client.KakaoMapClient;
import com.jinju.festival.infrastructure.kakao.dto.KakaoAddressResponse;
import com.jinju.festival.infrastructure.kakao.dto.KakaoCoordinate;
import com.jinju.festival.infrastructure.kakao.dto.KakaoDirectionsResponse;
import com.jinju.festival.infrastructure.kakao.dto.KakaoKeywordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMapService {

    private final KakaoMapClient kakaoMapClient;

    /**
     * 주소로 위경도 좌표 검색 (Kakao Geocoding)
     */
    public Optional<KakaoCoordinate> getCoordinatesByAddress(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        KakaoAddressResponse response = kakaoMapClient.searchAddress(address);
        if (response != null && !response.getDocuments().isEmpty()) {
            KakaoAddressResponse.Document doc = response.getDocuments().get(0);
            return Optional.of(KakaoCoordinate.builder()
                    .address(doc.getAddressName())
                    .longitude(Double.parseDouble(doc.getX()))
                    .latitude(Double.parseDouble(doc.getY()))
                    .name(doc.getAddressName())
                    .build());
        }

        return Optional.empty();
    }

    /**
     * 장소 키워드로 좌표 검색
     */
    public List<KakaoCoordinate> searchPlaceByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }

        KakaoKeywordResponse response = kakaoMapClient.searchKeyword(keyword);
        if (response != null && !response.getDocuments().isEmpty()) {
            List<KakaoCoordinate> results = new ArrayList<>();
            for (KakaoKeywordResponse.Document doc : response.getDocuments()) {
                results.add(KakaoCoordinate.builder()
                        .name(doc.getPlaceName())
                        .address(doc.getRoadAddressName() != null ? doc.getRoadAddressName() : doc.getAddressName())
                        .longitude(Double.parseDouble(doc.getX()))
                        .latitude(Double.parseDouble(doc.getY()))
                        .build());
            }
            return results;
        }

        return Collections.emptyList();
    }

    /**
     * 두 좌표 간 카카오모빌리티 길찾기 및 도로 구간 거리/좌표 수집
     */
    public KakaoDirectionsResponse getDirections(Double originLon, Double originLat, Double destLon, Double destLat) {
        return kakaoMapClient.getDirections(originLon, originLat, destLon, destLat);
    }
}
