package com.jinju.festival.infrastructure.dataportal.service;

import com.jinju.festival.domain.festival.entity.Festival;
import com.jinju.festival.domain.festival.repository.FestivalRepository;
import com.jinju.festival.domain.parking.entity.Parking;
import com.jinju.festival.domain.parking.entity.ParkingStatusHistory;
import com.jinju.festival.domain.parking.repository.ParkingRepository;
import com.jinju.festival.domain.parking.repository.ParkingStatusHistoryRepository;
import com.jinju.festival.infrastructure.dataportal.client.DataPortalParkingClient;
import com.jinju.festival.infrastructure.dataportal.dto.DataPortalParkingItem;
import com.jinju.festival.infrastructure.kakao.dto.KakaoCoordinate;
import com.jinju.festival.infrastructure.kakao.service.KakaoMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataPortalParkingSyncService {

    private final DataPortalParkingClient dataPortalParkingClient;
    private final ParkingRepository parkingRepository;
    private final ParkingStatusHistoryRepository parkingStatusHistoryRepository;
    private final KakaoMapService kakaoMapService;
    private final FestivalRepository festivalRepository;

    @Transactional
    public List<ParkingStatusHistory> syncParkingStatus() {
        List<DataPortalParkingItem> items = dataPortalParkingClient.fetchRealtimeParkingStatus();
        List<ParkingStatusHistory> savedHistories = new ArrayList<>();

        Festival defaultFestival = festivalRepository.findAll().stream().findFirst().orElse(null);

        for (DataPortalParkingItem item : items) {
            Parking parking = resolveAndGeocodeParking(item, defaultFestival);
            if (parking == null) {
                log.warn("Unable to resolve or register parking for item: {}", item.getPrkplceNo());
                continue;
            }

            int totalSpace = (parking.getTotalSpace() != null && parking.getTotalSpace() > 0)
                    ? parking.getTotalSpace()
                    : (item.getTotalPrkcm() != null && item.getTotalPrkcm() > 0 ? item.getTotalPrkcm() : 100);

            int currentCount = item.getPrkcm() != null ? item.getPrkcm() : 0;
            int usedDisSpace = item.getDisPrkcm() != null ? item.getDisPrkcm() : 0;
            int availableSpaces = Math.max(0, totalSpace - currentCount);

            BigDecimal occupancyRate = BigDecimal.valueOf((double) currentCount / totalSpace * 100)
                    .setScale(2, RoundingMode.HALF_UP);

            boolean isFull = availableSpaces <= 0 || occupancyRate.doubleValue() >= 100.0;
            String congestionStatus;
            if (isFull) {
                congestionStatus = "만차";
            } else if (occupancyRate.doubleValue() >= 85.0) {
                congestionStatus = "혼잡";
            } else if (occupancyRate.doubleValue() >= 50.0) {
                congestionStatus = "보통";
            } else {
                congestionStatus = "원활";
            }

            ParkingStatusHistory history = parkingStatusHistoryRepository.save(
                    ParkingStatusHistory.builder()
                            .parking(parking)
                            .currentParkingCount(currentCount)
                            .usedSpace(currentCount)
                            .usedDisSpace(usedDisSpace)
                            .availableSpaces(availableSpaces)
                            .occupancyRate(occupancyRate)
                            .isFull(isFull)
                            .congestionStatus(congestionStatus)
                            .build()
            );

            savedHistories.add(history);
        }

        log.info("Successfully synced and persisted {} parking status records with Kakao geocoding.", savedHistories.size());
        return savedHistories;
    }

    /**
     * 공공데이터 주차장 항목을 DB 엔티티와 매핑하고,
     * 주소 정보가 있을 경우 카카오맵 REST API를 통해 위경도 좌표로 변환하여 갱신합니다.
     */
    private Parking resolveAndGeocodeParking(DataPortalParkingItem item, Festival festival) {
        String externalCode = item.getPrkplceNo();
        String address = item.getEffectiveAddress();
        String parkingName = item.getPrkplceNm() != null ? item.getPrkplceNm() : "공공데이터 주차장";

        Optional<Parking> optionalParking = Optional.empty();
        if (externalCode != null && !externalCode.isBlank()) {
            optionalParking = parkingRepository.findByExternalCode(externalCode);
        }
        if (optionalParking.isEmpty()) {
            optionalParking = parkingRepository.findByName(parkingName);
        }

        if (optionalParking.isPresent()) {
            Parking existingParking = optionalParking.get();

            if (externalCode != null && existingParking.getExternalCode() == null) {
                existingParking.updateExternalCode(externalCode);
            }

            // 주차장 주소가 새로 제공되었고 기존 주소가 없으면 업데이트
            if ((existingParking.getAddress() == null || existingParking.getAddress().isBlank()) && address != null) {
                existingParking.updateAddress(address);
            }

            // 위도/경도가 누락되어 있는 경우 카카오맵 API로 변환하여 보정
            if (existingParking.getLatitude() == null || existingParking.getLongitude() == null) {
                geocodeAndApplyCoordinates(existingParking, address != null ? address : existingParking.getAddress(), parkingName);
            }

            return existingParking;
        }

        // DB에 없는 신규 주차장인 경우: Auto-increment BIGINT ID로 새로 저장
        KakaoCoordinate coordinate = fetchCoordinatesFromKakao(address, parkingName);
        BigDecimal lat = coordinate != null ? BigDecimal.valueOf(coordinate.getLatitude()) : null;
        BigDecimal lon = coordinate != null ? BigDecimal.valueOf(coordinate.getLongitude()) : null;

        Parking newParking = Parking.builder()
                .externalCode(externalCode)
                .festival(festival)
                .name(parkingName)
                .address(address != null ? address : (coordinate != null ? coordinate.getAddress() : null))
                .parkingType(item.getPrkplceEntrc() != null ? item.getPrkplceEntrc() : "공영주차장")
                .latitude(lat)
                .longitude(lon)
                .totalSpace(item.getTotalPrkcm() != null ? item.getTotalPrkcm() : 100)
                .disSpace(item.getDisPrkcm() != null ? item.getDisPrkcm() : 0)
                .build();

        return parkingRepository.save(newParking);
    }

    /**
     * 주소(1차) 또는 주차장 키워드(2차)를 카카오맵 REST API에 전달하여 위경도 좌표를 조회 및 엔티티에 반영
     */
    private void geocodeAndApplyCoordinates(Parking parking, String address, String parkingName) {
        KakaoCoordinate coordinate = fetchCoordinatesFromKakao(address, parkingName);
        if (coordinate != null) {
            log.info("Geocoded parking '{}' to [lat: {}, lon: {}] via Kakao Map API",
                    parking.getName(), coordinate.getLatitude(), coordinate.getLongitude());
            parking.updateCoordinates(
                    BigDecimal.valueOf(coordinate.getLatitude()),
                    BigDecimal.valueOf(coordinate.getLongitude())
            );
        }
    }

    private KakaoCoordinate fetchCoordinatesFromKakao(String address, String parkingName) {
        // 1. 주소로 카카오 Geocoding 검색
        if (address != null && !address.isBlank()) {
            Optional<KakaoCoordinate> coordOpt = kakaoMapService.getCoordinatesByAddress(address);
            if (coordOpt.isPresent()) {
                return coordOpt.get();
            }
        }

        // 2. 주소 검색 실패 또는 미제공 시 주차장명으로 카카오 키워드 장소 검색
        if (parkingName != null && !parkingName.isBlank()) {
            List<KakaoCoordinate> placeCoords = kakaoMapService.searchPlaceByKeyword(parkingName);
            if (!placeCoords.isEmpty()) {
                return placeCoords.get(0);
            }

            // '진주 + 주차장명'으로 재시도
            List<KakaoCoordinate> jinjuCoords = kakaoMapService.searchPlaceByKeyword("진주 " + parkingName);
            if (!jinjuCoords.isEmpty()) {
                return jinjuCoords.get(0);
            }
        }

        return null;
    }
}
