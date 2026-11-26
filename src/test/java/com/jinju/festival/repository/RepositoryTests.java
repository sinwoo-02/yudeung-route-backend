package com.jinju.festival.repository;

import com.jinju.festival.domain.festival.entity.Festival;
import com.jinju.festival.domain.festival.repository.FestivalRepository;
import com.jinju.festival.domain.parking.entity.Parking;
import com.jinju.festival.domain.parking.entity.ParkingStatusHistory;
import com.jinju.festival.domain.parking.repository.ParkingRepository;
import com.jinju.festival.domain.parking.repository.ParkingStatusHistoryRepository;
import com.jinju.festival.domain.restroom.entity.Restroom;
import com.jinju.festival.domain.restroom.repository.RestroomRepository;
import com.jinju.festival.domain.route.entity.RouteEdge;
import com.jinju.festival.domain.route.entity.RouteNode;
import com.jinju.festival.domain.route.repository.RouteEdgeRepository;
import com.jinju.festival.domain.route.repository.RouteNodeRepository;
import com.jinju.festival.domain.zone.entity.TouristZone;
import com.jinju.festival.domain.zone.entity.ZoneCongestionHistory;
import com.jinju.festival.domain.zone.repository.TouristZoneRepository;
import com.jinju.festival.domain.zone.repository.ZoneCongestionHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@SuppressWarnings("null")
class RepositoryTests {

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private TouristZoneRepository touristZoneRepository;

    @Autowired
    private ZoneCongestionHistoryRepository zoneCongestionHistoryRepository;

    @Autowired
    private RouteNodeRepository routeNodeRepository;

    @Autowired
    private RouteEdgeRepository routeEdgeRepository;

    @Autowired
    private RestroomRepository restroomRepository;

    @Autowired
    private ParkingRepository parkingRepository;

    @Autowired
    private ParkingStatusHistoryRepository parkingStatusHistoryRepository;

    @Test
    @DisplayName("초기 시드 데이터 및 전체 엔티티 매핑 검증 (주차장 테이블은 빈 상태로 초기화)")
    void verifyInitialSeedData() {
        // Festivals
        List<Festival> festivals = festivalRepository.findAll();
        assertThat(festivals).isNotEmpty();
        assertThat(festivals.get(0).getName()).contains("진주남강유등축제");
        assertThat(festivals.get(0).getCreatedAt()).isNotNull();

        // TouristZones (1 ~ 33)
        List<TouristZone> zones = touristZoneRepository.findAll();
        assertThat(zones).hasSize(33);
        assertThat(zones.get(0).getLatitude()).isNotNull();
        assertThat(zones.get(0).getLongitude()).isNotNull();
        assertThat(zones.get(0).getCoverageRadiusMeters()).isEqualTo(50);

        // ZoneCongestionHistory
        List<ZoneCongestionHistory> congestionHistories = zoneCongestionHistoryRepository.findAll();
        assertThat(congestionHistories).hasSize(4);

        // RouteNodes (101 ~ 224, 총 124개)
        List<RouteNode> nodes = routeNodeRepository.findAll();
        assertThat(nodes).hasSize(124);
        assertThat(routeNodeRepository.findById(101L)).isPresent();
        assertThat(routeNodeRepository.findById(224L)).isPresent();

        // RouteEdges (총 142개 양방향 간선)
        List<RouteEdge> edges = routeEdgeRepository.findAll();
        assertThat(edges).hasSize(142);

        // Restrooms (총 10개 화장실)
        List<Restroom> restrooms = restroomRepository.findAll();
        assertThat(restrooms).hasSize(10);
        assertThat(restrooms.get(0).getFestival().getFestivalId()).isEqualTo(festivals.get(0).getFestivalId());

        // Parkings: 초기 시드에서는 비어있고 공공데이터 API 스케줄러로 동적 수집됨
        List<Parking> parkings = parkingRepository.findAll();
        assertThat(parkings).isEmpty();
    }

    @Test
    @DisplayName("주차장별 최신 상태 이력 Custom Query 검증")
    void testLatestParkingStatusQueries() {
        Festival festival = festivalRepository.findAll().get(0);
        Parking parking = parkingRepository.save(
                Parking.builder()
                        .festival(festival)
                        .name("테스트 주차장")
                        .address("진주시 남강로 1")
                        .totalSpace(100)
                        .build()
        );

        parkingStatusHistoryRepository.save(
                ParkingStatusHistory.builder()
                        .parking(parking)
                        .currentParkingCount(40)
                        .availableSpaces(60)
                        .occupancyRate(new BigDecimal("40.00"))
                        .isFull(false)
                        .congestionStatus("원활")
                        .build()
        );

        List<ParkingStatusHistory> latestStatuses = parkingStatusHistoryRepository.findLatestStatusForAllParkings();
        assertThat(latestStatuses).isNotEmpty();

        Optional<ParkingStatusHistory> latest = parkingStatusHistoryRepository.findLatestByParkingId(parking.getParkingId());
        assertThat(latest).isPresent();
        assertThat(latest.get().getParking().getParkingId()).isEqualTo(parking.getParkingId());
        assertThat(latest.get().getCurrentParkingCount()).isEqualTo(40);
    }

    @Test
    @DisplayName("관광구역별 최신 혼잡도 이력 Custom Query 검증")
    void testLatestZoneCongestionQueries() {
        List<ZoneCongestionHistory> latestCongestions = zoneCongestionHistoryRepository.findLatestCongestionForAllZones();
        assertThat(latestCongestions).hasSize(4);

        TouristZone firstZone = touristZoneRepository.findAll().get(0);
        Optional<ZoneCongestionHistory> zoneLatest = zoneCongestionHistoryRepository.findLatestByZoneId(firstZone.getZoneId());
        assertThat(zoneLatest).isPresent();
        assertThat(zoneLatest.get().getTouristZone().getZoneId()).isEqualTo(firstZone.getZoneId());
    }

    @Test
    @DisplayName("경로 탐색 노드 및 간선 연관관계 조회 검증")
    void testRouteNodesAndEdges() {
        RouteNode startNode = routeNodeRepository.findById(101L).orElseThrow();
        List<RouteEdge> foundEdges = routeEdgeRepository.findByStartNode_NodeIdOrEndNode_NodeId(
                startNode.getNodeId(), startNode.getNodeId());
        assertThat(foundEdges).isNotEmpty();
    }
}
