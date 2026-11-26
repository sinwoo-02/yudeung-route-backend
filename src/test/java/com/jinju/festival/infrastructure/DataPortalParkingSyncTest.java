package com.jinju.festival.infrastructure;

import com.jinju.festival.domain.parking.entity.ParkingStatusHistory;
import com.jinju.festival.domain.parking.repository.ParkingStatusHistoryRepository;
import com.jinju.festival.infrastructure.dataportal.client.DataPortalParkingClient;
import com.jinju.festival.infrastructure.dataportal.dto.DataPortalParkingItem;
import com.jinju.festival.infrastructure.dataportal.service.DataPortalParkingSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DataPortalParkingSyncTest {

    @Autowired
    private DataPortalParkingClient dataPortalParkingClient;

    @Autowired
    private DataPortalParkingSyncService dataPortalParkingSyncService;

    @Autowired
    private ParkingStatusHistoryRepository parkingStatusHistoryRepository;

    @Test
    @DisplayName("공공데이터포털 클라이언트 호출 검증")
    void testClientFetch() {
        List<DataPortalParkingItem> items = dataPortalParkingClient.fetchRealtimeParkingStatus();
        assertThat(items).isNotEmpty();
        assertThat(items.get(0).getPrkplceNo()).isNotNull();
        assertThat(items.get(0).getTotalPrkcm()).isGreaterThan(0);
    }

    @Test
    @DisplayName("공공데이터 파싱 및 ParkingStatusHistory 저장 동기화 서비스 검증")
    void testSyncParkingStatus() {
        long initialCount = parkingStatusHistoryRepository.count();

        List<ParkingStatusHistory> savedList = dataPortalParkingSyncService.syncParkingStatus();
        assertThat(savedList).isNotEmpty();

        long newCount = parkingStatusHistoryRepository.count();
        assertThat(newCount).isGreaterThan(initialCount);

        ParkingStatusHistory first = savedList.get(0);
        assertThat(first.getParking()).isNotNull();
        assertThat(first.getOccupancyRate()).isNotNull();
        assertThat(first.getCongestionStatus()).isNotBlank();
        assertThat(first.getAvailableSpaces()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("카카오맵 지오코딩: 주소 기반 위경도 좌표 자동 동기화 검증")
    void testGeocodingForParking() {
        List<ParkingStatusHistory> savedList = dataPortalParkingSyncService.syncParkingStatus();
        assertThat(savedList).isNotEmpty();

        for (ParkingStatusHistory history : savedList) {
            assertThat(history.getParking().getLatitude())
                    .as("주차장 위도가 정상적으로 설정되어야 함")
                    .isNotNull();
            assertThat(history.getParking().getLongitude())
                    .as("주차장 경도가 정상적으로 설정되어야 함")
                    .isNotNull();
        }
    }
}
