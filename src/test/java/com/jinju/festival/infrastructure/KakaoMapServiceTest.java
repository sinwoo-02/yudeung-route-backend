package com.jinju.festival.infrastructure;

import com.jinju.festival.infrastructure.kakao.dto.KakaoCoordinate;
import com.jinju.festival.infrastructure.kakao.dto.KakaoDirectionsResponse;
import com.jinju.festival.infrastructure.kakao.service.KakaoMapService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class KakaoMapServiceTest {

    @Autowired
    private KakaoMapService kakaoMapService;

    @Test
    @DisplayName("주소-좌표 변환 API 검증")
    void testGetCoordinatesByAddress() {
        Optional<KakaoCoordinate> coordinate = kakaoMapService.getCoordinatesByAddress("경상남도 진주시 남강로 626");
        assertThat(coordinate).isPresent();
        assertThat(coordinate.get().getLatitude()).isNotNull();
        assertThat(coordinate.get().getLongitude()).isNotNull();
        assertThat(coordinate.get().getAddress()).contains("진주시");
    }

    @Test
    @DisplayName("키워드 장소 검색 API 검증")
    void testSearchPlaceByKeyword() {
        List<KakaoCoordinate> places = kakaoMapService.searchPlaceByKeyword("촉석루");
        assertThat(places).isNotEmpty();
        assertThat(places.get(0).getName()).contains("촉석루");
        assertThat(places.get(0).getLatitude()).isNotNull();
    }

    @Test
    @DisplayName("카카오모빌리티 길찾기 및 도로 구간/노드 수집 API 검증")
    void testGetDirections() {
        // 진주성 공영주차장 -> 촉석문
        Double originLon = 128.0807;
        Double originLat = 35.1896;
        Double destLon = 128.0841;
        Double destLat = 35.1884;

        KakaoDirectionsResponse directions = kakaoMapService.getDirections(originLon, originLat, destLon, destLat);
        assertThat(directions).isNotNull();
        assertThat(directions.getRoutes()).isNotEmpty();

        KakaoDirectionsResponse.Route route = directions.getRoutes().get(0);
        assertThat(route.getSummary().getDistance()).isGreaterThan(0);
        assertThat(route.getSections()).isNotEmpty();
        assertThat(route.getSections().get(0).getRoads()).isNotEmpty();
        assertThat(route.getSections().get(0).getRoads().get(0).getVertexes()).isNotEmpty();
    }
}
