package com.jinju.festival.domain.zone.service;

import com.jinju.festival.domain.zone.dto.TouristZoneDetailResponse;
import com.jinju.festival.domain.zone.dto.TouristZoneDto;
import com.jinju.festival.domain.zone.dto.ZoneCongestionHistoryDto;
import com.jinju.festival.domain.zone.entity.TouristZone;
import com.jinju.festival.domain.zone.entity.ZoneCongestionHistory;
import com.jinju.festival.domain.zone.repository.TouristZoneRepository;
import com.jinju.festival.domain.zone.repository.ZoneCongestionHistoryRepository;
import com.jinju.festival.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TouristZoneService {

    private final TouristZoneRepository touristZoneRepository;
    private final ZoneCongestionHistoryRepository zoneCongestionHistoryRepository;

    /**
     * 전체 축제 관광/도로 구역 목록 및 실시간 CCTV 혼잡도 조회
     */
    public List<TouristZoneDto> getAllZonesWithLatestCongestion() {
        List<TouristZone> zones = touristZoneRepository.findAll();
        List<ZoneCongestionHistory> latestCongestions = zoneCongestionHistoryRepository.findLatestCongestionForAllZones();

        Map<Long, ZoneCongestionHistory> congestionMap = new HashMap<>();
        for (ZoneCongestionHistory history : latestCongestions) {
            if (history.getTouristZone() != null) {
                congestionMap.put(history.getTouristZone().getZoneId(), history);
            }
        }

        return zones.stream()
                .map(zone -> TouristZoneDto.from(zone, congestionMap.get(zone.getZoneId())))
                .toList();
    }

    /**
     * 특정 관광 구역 상세 및 실시간 혼잡도/이력 조회
     */
    public TouristZoneDetailResponse getZoneDetail(Long zoneId) {
        TouristZone zone = touristZoneRepository.findById(zoneId)
                .orElseThrow(() -> new EntityNotFoundException("해당 관광 구역을 찾을 수 없습니다: " + zoneId));

        List<ZoneCongestionHistory> histories = zoneCongestionHistoryRepository.findByTouristZone_ZoneIdOrderByCreatedAtDesc(zoneId);
        ZoneCongestionHistory latestHistory = histories.isEmpty() ? null : histories.get(0);

        List<ZoneCongestionHistoryDto> historyDtos = histories.stream()
                .limit(20)
                .map(ZoneCongestionHistoryDto::from)
                .toList();

        return TouristZoneDetailResponse.builder()
                .zone(TouristZoneDto.from(zone, latestHistory))
                .recentCongestionHistories(historyDtos)
                .build();
    }

    /**
     * AI 서버 영상 분석 기반 혼잡도 데이터 저장 (영상당 1건 저장)
     */
    @Transactional
    public com.jinju.festival.domain.zone.dto.AiCongestionSaveResponse saveAiCongestionReports(
            List<com.jinju.festival.domain.zone.dto.AiVideoCongestionReportRequest> reports) {

        if (reports == null || reports.isEmpty()) {
            return com.jinju.festival.domain.zone.dto.AiCongestionSaveResponse.builder()
                    .savedCount(0)
                    .savedHistories(List.of())
                    .build();
        }

        java.util.List<ZoneCongestionHistory> historiesToSave = new java.util.ArrayList<>();

        for (var report : reports) {
            Long zoneId = report.getResolvedZoneId();
            if (zoneId == null) {
                log.warn("CCTV 영상 분석 데이터에 zone_id가 누락되어 건너뜁니다: file={}", report.getFileName());
                continue;
            }

            TouristZone zone = touristZoneRepository.findById(zoneId)
                    .orElseThrow(() -> new EntityNotFoundException("구역 ID에 해당하는 관광 구역을 찾을 수 없습니다: " + zoneId));

            Integer peopleCount = report.getResolvedPeopleCount();
            String rawStatus = report.getResolvedOverallStatus();
            String congestionLevel = mapToKoreanCongestionLevel(rawStatus);

            ZoneCongestionHistory history = ZoneCongestionHistory.builder()
                    .touristZone(zone)
                    .peopleCount(peopleCount)
                    .congestionLevel(congestionLevel)
                    .build();

            historiesToSave.add(history);
        }

        List<ZoneCongestionHistory> saved = zoneCongestionHistoryRepository.saveAll(historiesToSave);

        List<ZoneCongestionHistoryDto> dtos = saved.stream()
                .map(ZoneCongestionHistoryDto::from)
                .toList();

        log.info("AI CCTV 영상 혼잡도 데이터 저장 완료: 총 {}건 저장", dtos.size());

        return com.jinju.festival.domain.zone.dto.AiCongestionSaveResponse.builder()
                .savedCount(dtos.size())
                .savedHistories(dtos)
                .build();
    }

    /**
     * AI 분석 status 문자열을 백엔드 혼잡도 한글 레벨로 변환
     */
    public String mapToKoreanCongestionLevel(String status) {
        if (status == null || status.isBlank()) {
            return "원활";
        }

        return switch (status.trim().toUpperCase()) {
            case "NORMAL", "원활", "LOW", "GREEN", "SMOOTH" -> "원활";
            case "MODERATE", "보통", "MEDIUM", "YELLOW", "CAUTION" -> "보통";
            case "CONGESTED", "혼잡", "HIGH", "ORANGE", "WARNING" -> "혼잡";
            case "VERY_CONGESTED", "매우 혼잡", "CRITICAL", "RED", "VERY_CROWDED", "CROWDED" -> "매우 혼잡";
            default -> {
                if (status.contains("혼잡") && status.contains("매우")) yield "매우 혼잡";
                if (status.contains("혼잡")) yield "혼잡";
                if (status.contains("보통")) yield "보통";
                if (status.contains("원활")) yield "원활";
                yield "보통";
            }
        };
    }
}
