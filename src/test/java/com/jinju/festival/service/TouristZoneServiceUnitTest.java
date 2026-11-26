package com.jinju.festival.service;

import com.jinju.festival.domain.zone.dto.TouristZoneDetailResponse;
import com.jinju.festival.domain.zone.dto.TouristZoneDto;
import com.jinju.festival.domain.zone.entity.TouristZone;
import com.jinju.festival.domain.zone.entity.ZoneCongestionHistory;
import com.jinju.festival.domain.zone.repository.TouristZoneRepository;
import com.jinju.festival.domain.zone.repository.ZoneCongestionHistoryRepository;
import com.jinju.festival.domain.zone.service.TouristZoneService;
import com.jinju.festival.global.error.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TouristZoneServiceUnitTest {

    @Mock
    private TouristZoneRepository touristZoneRepository;

    @Mock
    private ZoneCongestionHistoryRepository zoneCongestionHistoryRepository;

    @InjectMocks
    private TouristZoneService touristZoneService;

    @Test
    @DisplayName("관광 구역 목록 및 실시간 혼잡도 매핑 단위 테스트")
    void testGetAllZonesWithLatestCongestion() {
        TouristZone zone = TouristZone.builder()
                .zoneId(1L)
                .zoneName("촉석루 구역")
                .build();

        ZoneCongestionHistory history = ZoneCongestionHistory.builder()
                .touristZone(zone)
                .peopleCount(500)
                .congestionLevel("혼잡")
                .build();

        given(touristZoneRepository.findAll()).willReturn(List.of(zone));
        given(zoneCongestionHistoryRepository.findLatestCongestionForAllZones()).willReturn(List.of(history));

        List<TouristZoneDto> results = touristZoneService.getAllZonesWithLatestCongestion();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getZoneName()).isEqualTo("촉석루 구역");
        assertThat(results.get(0).getPeopleCount()).isEqualTo(500);
        assertThat(results.get(0).getCongestionLevel()).isEqualTo("혼잡");
        assertThat(results.get(0).getCongestionMultiplier()).isEqualTo(1.8);
    }

    @Test
    @DisplayName("존재하지 않는 관광 구역 상세 조회 시 EntityNotFoundException 발생 단위 테스트")
    void testGetZoneDetailNotFound() {
        given(touristZoneRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> touristZoneService.getZoneDetail(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("해당 관광 구역을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("관광 구역 상세 및 최근 혼잡도 이력 조회 단위 테스트")
    void testGetZoneDetailSuccess() {
        TouristZone zone = TouristZone.builder()
                .zoneId(1L)
                .zoneName("촉석루 구역")
                .build();

        ZoneCongestionHistory history = ZoneCongestionHistory.builder()
                .historyId(10L)
                .touristZone(zone)
                .peopleCount(300)
                .congestionLevel("보통")
                .build();

        given(touristZoneRepository.findById(1L)).willReturn(Optional.of(zone));
        given(zoneCongestionHistoryRepository.findByTouristZone_ZoneIdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(history));

        TouristZoneDetailResponse response = touristZoneService.getZoneDetail(1L);

        assertThat(response.getZone().getZoneId()).isEqualTo(1L);
        assertThat(response.getRecentCongestionHistories()).hasSize(1);
        assertThat(response.getRecentCongestionHistories().get(0).getPeopleCount()).isEqualTo(300);
    }

    @Test
    @DisplayName("AI 영상 분석 혼잡도 데이터 1건 저장 및 상태 매핑 단위 테스트")
    void testSaveAiCongestionReports() {
        TouristZone zone = TouristZone.builder()
                .zoneId(1L)
                .zoneName("촉석루 구역")
                .build();

        var request = com.jinju.festival.domain.zone.dto.AiVideoCongestionReportRequest.builder()
                .zoneId(1L)
                .fileName("test.mp4")
                .avgPersonCount(44.45)
                .maxPersonCount(50)
                .overallStatus("NORMAL")
                .build();

        ZoneCongestionHistory savedHistory = ZoneCongestionHistory.builder()
                .historyId(100L)
                .touristZone(zone)
                .peopleCount(44)
                .congestionLevel("원활")
                .build();

        given(touristZoneRepository.findById(1L)).willReturn(Optional.of(zone));
        given(zoneCongestionHistoryRepository.saveAll(org.mockito.ArgumentMatchers.anyList()))
                .willReturn(List.of(savedHistory));

        var response = touristZoneService.saveAiCongestionReports(List.of(request));

        assertThat(response.getSavedCount()).isEqualTo(1);
        assertThat(response.getSavedHistories().get(0).getPeopleCount()).isEqualTo(44);
        assertThat(response.getSavedHistories().get(0).getCongestionLevel()).isEqualTo("원활");
    }

    @Test
    @DisplayName("AI 분석 영문 상태값 한글 혼잡도 매핑 로직 단위 테스트")
    void testMapToKoreanCongestionLevel() {
        assertThat(touristZoneService.mapToKoreanCongestionLevel("NORMAL")).isEqualTo("원활");
        assertThat(touristZoneService.mapToKoreanCongestionLevel("CAUTION")).isEqualTo("보통");
        assertThat(touristZoneService.mapToKoreanCongestionLevel("CONGESTED")).isEqualTo("혼잡");
        assertThat(touristZoneService.mapToKoreanCongestionLevel("VERY_CONGESTED")).isEqualTo("매우 혼잡");
        assertThat(touristZoneService.mapToKoreanCongestionLevel(null)).isEqualTo("원활");
    }
}
