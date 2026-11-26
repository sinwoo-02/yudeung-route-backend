package com.jinju.festival.service;

import com.jinju.festival.domain.parking.dto.ParkingDetailResponse;
import com.jinju.festival.domain.parking.dto.ParkingDto;
import com.jinju.festival.domain.parking.entity.Parking;
import com.jinju.festival.domain.parking.entity.ParkingStatusHistory;
import com.jinju.festival.domain.parking.repository.ParkingRepository;
import com.jinju.festival.domain.parking.repository.ParkingStatusHistoryRepository;
import com.jinju.festival.domain.parking.service.ParkingService;
import com.jinju.festival.global.error.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ParkingServiceUnitTest {

    @Mock
    private ParkingRepository parkingRepository;

    @Mock
    private ParkingStatusHistoryRepository parkingStatusHistoryRepository;

    @InjectMocks
    private ParkingService parkingService;

    @Test
    @DisplayName("주차장 목록 및 최신 상태 매핑 단위 테스트")
    void testGetAllParkingsWithLatestStatus() {
        Parking parking = Parking.builder()
                .parkingId(1L)
                .name("진주성 주차장")
                .totalSpace(100)
                .build();

        ParkingStatusHistory status = ParkingStatusHistory.builder()
                .parking(parking)
                .currentParkingCount(40)
                .availableSpaces(60)
                .occupancyRate(new BigDecimal("40.00"))
                .isFull(false)
                .congestionStatus("원활")
                .build();

        given(parkingRepository.findAll()).willReturn(List.of(parking));
        given(parkingStatusHistoryRepository.findLatestStatusForAllParkings()).willReturn(List.of(status));

        List<ParkingDto> results = parkingService.getAllParkingsWithLatestStatus();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("진주성 주차장");
        assertThat(results.get(0).getAvailableSpaces()).isEqualTo(60);
        assertThat(results.get(0).getCongestionStatus()).isEqualTo("원활");
    }

    @Test
    @DisplayName("존재하지 않는 주차장 상세 조회 시 EntityNotFoundException 발생 단위 테스트")
    void testGetParkingDetailNotFound() {
        given(parkingRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.getParkingDetail(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("해당 주차장을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주차장 상세 및 최근 이력 조회 단위 테스트")
    void testGetParkingDetailSuccess() {
        Parking parking = Parking.builder()
                .parkingId(1L)
                .name("진주성 주차장")
                .totalSpace(100)
                .build();

        ParkingStatusHistory status = ParkingStatusHistory.builder()
                .historyId(1L)
                .parking(parking)
                .currentParkingCount(95)
                .availableSpaces(5)
                .occupancyRate(new BigDecimal("95.00"))
                .isFull(false)
                .congestionStatus("혼잡")
                .build();

        given(parkingRepository.findById(1L)).willReturn(Optional.of(parking));
        given(parkingStatusHistoryRepository.findByParking_ParkingIdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(status));

        ParkingDetailResponse response = parkingService.getParkingDetail(1L);

        assertThat(response.getParking().getParkingId()).isEqualTo(1L);
        assertThat(response.getRecentStatusHistories()).hasSize(1);
        assertThat(response.getRecentStatusHistories().get(0).getCongestionStatus()).isEqualTo("혼잡");
    }
}
