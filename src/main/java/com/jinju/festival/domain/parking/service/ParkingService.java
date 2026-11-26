package com.jinju.festival.domain.parking.service;

import com.jinju.festival.domain.parking.dto.ParkingDetailResponse;
import com.jinju.festival.domain.parking.dto.ParkingDto;
import com.jinju.festival.domain.parking.dto.ParkingStatusHistoryDto;
import com.jinju.festival.domain.parking.entity.Parking;
import com.jinju.festival.domain.parking.entity.ParkingStatusHistory;
import com.jinju.festival.domain.parking.repository.ParkingRepository;
import com.jinju.festival.domain.parking.repository.ParkingStatusHistoryRepository;
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
public class ParkingService {

    private final ParkingRepository parkingRepository;
    private final ParkingStatusHistoryRepository parkingStatusHistoryRepository;

    /**
     * 전체 축제 주차장 목록 및 최신 실시간 잔여면수/혼잡도 조회
     */
    public List<ParkingDto> getAllParkingsWithLatestStatus() {
        List<Parking> parkings = parkingRepository.findAll();
        List<ParkingStatusHistory> latestStatuses = parkingStatusHistoryRepository.findLatestStatusForAllParkings();

        Map<Long, ParkingStatusHistory> statusMap = new HashMap<>();
        for (ParkingStatusHistory status : latestStatuses) {
            if (status.getParking() != null) {
                statusMap.put(status.getParking().getParkingId(), status);
            }
        }

        return parkings.stream()
                .map(parking -> ParkingDto.from(parking, statusMap.get(parking.getParkingId())))
                .toList();
    }

    /**
     * 특정 주차장 상세 및 실시간 상태/최근 이력 조회
     */
    public ParkingDetailResponse getParkingDetail(Long parkingId) {
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new EntityNotFoundException("해당 주차장을 찾을 수 없습니다: " + parkingId));

        List<ParkingStatusHistory> histories = parkingStatusHistoryRepository.findByParking_ParkingIdOrderByCreatedAtDesc(parkingId);
        ParkingStatusHistory latestStatus = histories.isEmpty() ? null : histories.get(0);

        List<ParkingStatusHistoryDto> historyDtos = histories.stream()
                .limit(20)
                .map(ParkingStatusHistoryDto::from)
                .toList();

        return ParkingDetailResponse.builder()
                .parking(ParkingDto.from(parking, latestStatus))
                .recentStatusHistories(historyDtos)
                .build();
    }
}
