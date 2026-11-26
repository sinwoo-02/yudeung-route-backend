package com.jinju.festival.global.init;

import com.jinju.festival.domain.festival.entity.Festival;
import com.jinju.festival.domain.festival.repository.FestivalRepository;
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
import com.jinju.festival.global.util.GeoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final FestivalRepository festivalRepository;
    private final TouristZoneRepository touristZoneRepository;
    private final ZoneCongestionHistoryRepository zoneCongestionHistoryRepository;
    private final RouteNodeRepository routeNodeRepository;
    private final RouteEdgeRepository routeEdgeRepository;
    private final RestroomRepository restroomRepository;

    public DataInitializer(FestivalRepository festivalRepository,
                           TouristZoneRepository touristZoneRepository,
                           ZoneCongestionHistoryRepository zoneCongestionHistoryRepository,
                           RouteNodeRepository routeNodeRepository,
                           RouteEdgeRepository routeEdgeRepository,
                           RestroomRepository restroomRepository) {
        this.festivalRepository = festivalRepository;
        this.touristZoneRepository = touristZoneRepository;
        this.zoneCongestionHistoryRepository = zoneCongestionHistoryRepository;
        this.routeNodeRepository = routeNodeRepository;
        this.routeEdgeRepository = routeEdgeRepository;
        this.restroomRepository = restroomRepository;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public void run(String... args) {
        if (festivalRepository.count() > 0) {
            log.info("Initial seed data already exists. Skipping initialization.");
            return;
        }

        log.info("Start initializing seed data for Jinju Namgang Yudeung Festival...");

        // 1. Festival (festival_id = 1)
        Festival savedFestival = festivalRepository.save(
                Festival.builder()
                        .name("2026 진주남강유등축제")
                        .startDate(LocalDate.of(2026, 10, 5))
                        .endDate(LocalDate.of(2026, 10, 20))
                        .location("진주 남강 일원 및 진주성")
                        .description("빛으로 밝히는 진주의 가을, 대한민국 대표 문화관광축제 진주남강유등축제")
                        .build()
        );

        // 2. Tourist Zones (CCTV 구역 1 ~ 33)
        List<TouristZone> savedZones = touristZoneRepository.saveAll(List.of(
                createZone(savedFestival, "촉석루 (중심 / 조선 메인 ZONE)", "35.18948349", "128.0818568", 50),
                createZone(savedFestival, "영남포정사문루 (나)", "35.18961059", "128.079229", 40),
                createZone(savedFestival, "공북문 잔디광장 (K-풍의 ZONE / 다)", "35.19015689", "128.0801255", 60),
                createZone(savedFestival, "캐릭터 테마랜드 (라)", "35.18927916", "128.0800096", 45),
                createZone(savedFestival, "국립진주박물관 앞 잔디광장 (마)", "35.18899861", "128.0793801", 55),
                createZone(savedFestival, "화유나무 & 달 군락 (바 / 진주성카페 테라스)", "35.18917917", "128.0780815", 45),
                createZone(savedFestival, "송포 브이로그 (사 / 호국사 뒤 성곽길)", "35.18934199", "128.0762419", 45),
                createZone(savedFestival, "북장대 (중심 / 성곽 조망지)", "35.19008666", "128.0781456", 40),
                createZone(savedFestival, "서장대 (중심 / 서문 수변 구역)", "35.18711356", "128.0754769", 45),
                createZone(savedFestival, "창렬사 (중심 일대)", "35.18800751", "128.0760514", 40),
                createZone(savedFestival, "남강의암 (수변 밀집 위험구역)", "35.18923485", "128.0821966", 30),
                createZone(savedFestival, "음악분수대 광장 (타이포아트 IN 한글)", "35.18595928", "128.0755468", 70),
                createZone(savedFestival, "진주대첩역사공원", "35.19051721", "128.0836793", 80),
                createZone(savedFestival, "신안둔치 주차장 광장 (우주테마파크)", "35.18366527", "128.075061", 70),
                createZone(savedFestival, "남가람공원 (중심)", "35.18724358", "128.082129", 60),
                createZone(savedFestival, "물빛나루다리 (2부교 통행구역)", "35.18193517", "128.0753963", 40),
                createZone(savedFestival, "용다리전설다리 (1부교 통행구역)", "35.18666824", "128.0760186", 40),
                createZone(savedFestival, "배다리 (3부교 통행구역)", "35.18906666", "128.08387", 40),
                createZone(savedFestival, "천수교 (1부교 방면 / 서측 보행로)", "35.1849802", "128.0766921", 45),
                createZone(savedFestival, "천수교 (2부교 방면 / 동측 보행로)", "35.18477588", "128.0766098", 45),
                createZone(savedFestival, "진주교 (1부교 방면 / 서측 보행로)", "35.18942084", "128.0858358", 45),
                createZone(savedFestival, "진주교 (4부교 방면 / 동측 보행로)", "35.18945474", "128.0860751", 45),
                createZone(savedFestival, "별빛윤슬다리 통행구역 (4부교)", "35.18969384", "128.0881808", 40),
                createZone(savedFestival, "망경 수변 B·C존 (천수교 남단 하부~앞길1)", "35.18519699", "128.0784104", 45),
                createZone(savedFestival, "망경 수변 D·E존 (앞길2 부근)", "35.18659659", "128.0798918", 45),
                createZone(savedFestival, "망경 수변 F·G존 (수상무대/스탠드 일대 앞길3)", "35.1878522", "128.0826064", 50),
                createZone(savedFestival, "망경 수변 H존 (앞길4 부근)", "35.18835805", "128.0842435", 45),
                createZone(savedFestival, "망경 수변 진주교 하부 통로", "35.18870604", "128.0861447", 35),
                createZone(savedFestival, "유등공원 (중심)", "35.18292631", "128.0778369", 60),
                createZone(savedFestival, "망경동 남가람공원 대나무숲 산책로", "35.1874133", "128.0825512", 40),
                createZone(savedFestival, "옛우물", "35.18957969", "128.0806723", 30),
                createZone(savedFestival, "김시민 장군 전공비", "35.18986707", "128.0800311", 30),
                createZone(savedFestival, "김해김씨비각", "35.18910741", "128.0755087", 30)
        ));

        // 3. Zone Congestion History (혼잡도 이력 초기값)
        zoneCongestionHistoryRepository.saveAll(List.of(
                ZoneCongestionHistory.builder()
                        .touristZone(savedZones.get(0))
                        .peopleCount(750)
                        .congestionLevel("혼잡")
                        .build(),
                ZoneCongestionHistory.builder()
                        .touristZone(savedZones.get(1))
                        .peopleCount(380)
                        .congestionLevel("보통")
                        .build(),
                ZoneCongestionHistory.builder()
                        .touristZone(savedZones.get(2))
                        .peopleCount(180)
                        .congestionLevel("원활")
                        .build(),
                ZoneCongestionHistory.builder()
                        .touristZone(savedZones.get(3))
                        .peopleCount(90)
                        .congestionLevel("원활")
                        .build()
        ));

        // 4. Route Nodes (경로 탐색 네트워크 노드 101 ~ 224)
        List<RouteNode> savedNodes = routeNodeRepository.saveAll(List.of(
                createNode(101L, "물빛나루다리 북단", 35.18230592, 128.07437065, getZone(savedZones, 16)),
                createNode(102L, "물빛나루다리 중단", 35.18193517, 128.07539633, getZone(savedZones, 16)),
                createNode(103L, "물빛나루다리 남단", 35.18156441, 128.07642201, getZone(savedZones, 16)),
                createNode(104L, "2부교 남단 가는 길1", 35.18262072, 128.07698210, getZone(savedZones, 16)),
                createNode(105L, "2부교 남단 가는 길2", 35.18380409, 128.07744233, getZone(savedZones, 24)),
                createNode(106L, "천수교 남단 다리 밑", 35.18445822, 128.07787089, getZone(savedZones, 24)),
                createNode(107L, "천수교에서 진주교 가는 앞길1", 35.18519700, 128.07841035, getZone(savedZones, 24)),
                createNode(108L, "천수교 오른쪽 빨간도로 앞 갈림길", 35.18564630, 128.07881426, getZone(savedZones, 24)),
                createNode(109L, "천수교 오른쪽 빨간도로 뒷 갈림길", 35.18553635, 128.07901591, null),
                createNode(110L, "천수교에서 진주교 가는 앞길2", 35.18659659, 128.07989175, getZone(savedZones, 25)),
                createNode(111L, "천수교에서 진주교 가는 뒷길2", 35.18647103, 128.08007398, null),
                createNode(112L, "남가람공원 가는 길5", 35.18765962, 128.08247211, null),
                createNode(113L, "남가람공원 가는 길6", 35.18766581, 128.08278783, null),
                createNode(114L, "남가람공원 가는 길7", 35.18747428, 128.08253552, null),
                createNode(115L, "남가람공원 가는 길4", 35.18741330, 128.08255118, getZone(savedZones, 15)),
                createNode(116L, "남가람공원 (중심)", 35.18724358, 128.08212899, getZone(savedZones, 15)),
                createNode(117L, "천수교에서 진주교 가는 앞길3", 35.18785220, 128.08260641, getZone(savedZones, 26)),
                createNode(118L, "천수교에서 진주교 가는 뒷길3", 35.18772772, 128.08266788, null),
                createNode(119L, "천수교에서 진주교 가는 앞길4", 35.18835805, 128.08424348, getZone(savedZones, 27)),
                createNode(120L, "천수교에서 진주교 가는 뒷길4", 35.18824021, 128.08431877, null),
                createNode(121L, "남가람공원 가는 입구길", 35.18845431, 128.08506818, null),
                createNode(122L, "남가람공원 가는 길1", 35.18833490, 128.08506659, null),
                createNode(123L, "남가람공원 가는 길2", 35.18821208, 128.08469167, null),
                createNode(124L, "남가람공원 가는 길3", 35.18805915, 128.08465945, null),
                createNode(125L, "진주교 남단 다리 밑", 35.18870604, 128.08614471, getZone(savedZones, 28)),
                createNode(126L, "진주교 다리 밑 연결길", 35.18876761, 128.08606319, getZone(savedZones, 28)),
                createNode(127L, "진주교 우측편 갈림길", 35.18881492, 128.08656611, null),
                createNode(128L, "진주교 가는길1", 35.18876469, 128.08789389, null),
                createNode(129L, "진주교 가는길2", 35.18867188, 128.08794205, null),
                createNode(130L, "진주교 가는길3", 35.18853014, 128.08791821, null),
                createNode(131L, "진주교 가는길4", 35.18855122, 128.08707586, null),
                createNode(132L, "진주교 가는길5", 35.18828698, 128.08664142, null),
                createNode(133L, "진주교 가는길6", 35.18818691, 128.08523754, null),
                createNode(134L, "진주교 가는길7", 35.18797209, 128.08532251, null),
                createNode(135L, "진주교 가는길8", 35.18817278, 128.08631053, null),
                createNode(136L, "진주교 남단(4부교 방면)", 35.18845907, 128.08654764, getZone(savedZones, 22)),
                createNode(137L, "진주교 중단(4부교 방면)", 35.18945474, 128.08607508, getZone(savedZones, 22)),
                createNode(138L, "진주교 북단(4부교 방면)", 35.19056123, 128.08555731, getZone(savedZones, 22)),
                createNode(139L, "진주교 남단(1부교 방면)", 35.18838216, 128.08632979, getZone(savedZones, 21)),
                createNode(140L, "진주교 중단(1부교 방면)", 35.18942084, 128.08583583, getZone(savedZones, 21)),
                createNode(141L, "진주교 북단(1부교 방면)", 35.19053185, 128.08531538, getZone(savedZones, 21)),
                createNode(142L, "별빛윤슬다리 남단", 35.18898750, 128.08817408, getZone(savedZones, 23)),
                createNode(143L, "별빛윤슬다리 북단", 35.19040017, 128.08818742, getZone(savedZones, 23)),
                createNode(144L, "진주교 북단 4부교 가는길1", 35.19039298, 128.08572525, null),
                createNode(145L, "천수교 다리 올라가는 길1", 35.18473138, 128.07831912, null),
                createNode(146L, "천수교 다리 올라가는 길2", 35.18456897, 128.07833893, null),
                createNode(147L, "천수교 남단(1부교 방면)", 35.18461344, 128.07815289, getZone(savedZones, 19)),
                createNode(148L, "천수교 중단(1부교 방면)", 35.18498020, 128.07669211, getZone(savedZones, 19)),
                createNode(149L, "천수교 북단(1부교 방면)", 35.18531528, 128.07524463, getZone(savedZones, 19)),
                createNode(150L, "유등공원 가는길1", 35.18454021, 128.07878592, getZone(savedZones, 29)),
                createNode(151L, "유등공원 가는길2", 35.18444764, 128.07880665, getZone(savedZones, 29)),
                createNode(152L, "유등공원 가는길3", 35.18427506, 128.07870282, getZone(savedZones, 29)),
                createNode(153L, "유등공원 가는길4", 35.18420091, 128.07893239, getZone(savedZones, 29)),
                createNode(154L, "유등공원 가는길5", 35.18407364, 128.07880171, getZone(savedZones, 29)),
                createNode(155L, "유등공원 가는길6", 35.18406555, 128.07793981, getZone(savedZones, 29)),
                createNode(156L, "유등공원 가는길7", 35.18344214, 128.07760223, getZone(savedZones, 29)),
                createNode(157L, "유등공원 (중심)", 35.18292631, 128.07783694, getZone(savedZones, 29)),
                createNode(158L, "천수교 남단(2부교 방면)", 35.18443832, 128.07808196, getZone(savedZones, 20)),
                createNode(159L, "천수교 중단(2부교 방면)", 35.18477588, 128.07660982, getZone(savedZones, 20)),
                createNode(160L, "천수교 북단(2부교 방면)", 35.18507161, 128.07527984, getZone(savedZones, 20)),
                createNode(161L, "배다리 남단", 35.18835418, 128.08417207, getZone(savedZones, 18)),
                createNode(162L, "배다리 북단", 35.18977914, 128.08356793, getZone(savedZones, 18)),
                createNode(163L, "진주교와 배다리 사이 길", 35.19020479, 128.08484991, null),
                createNode(164L, "촉석문", 35.18976150, 128.08276897, getZone(savedZones, 1)),
                createNode(165L, "진주대첩역사공원", 35.19051721, 128.08367929, getZone(savedZones, 13)),
                createNode(166L, "신안둔치 주차장 광장", 35.18366527, 128.07506096, getZone(savedZones, 14)),
                createNode(167L, "천수교 북단 2부교 가는길1", 35.18430019, 128.07537123, null),
                createNode(168L, "천수교 북단 다리 밑", 35.18496412, 128.07571207, null),
                createNode(169L, "음악분수대 앞 강변", 35.18581893, 128.07612680, getZone(savedZones, 12)),
                createNode(170L, "용다리전설다리 남단", 35.18635234, 128.07594171, getZone(savedZones, 17)),
                createNode(171L, "용다리전설다리 북단", 35.18698414, 128.07609550, getZone(savedZones, 17)),
                createNode(172L, "강변1", 35.18742904, 128.07724865, null),
                createNode(173L, "강변2", 35.18825461, 128.07917536, null),
                createNode(174L, "강변3", 35.18934878, 128.08129780, null),
                createNode(175L, "촉석루 (중심)", 35.18948349, 128.08185677, getZone(savedZones, 1)),
                createNode(176L, "남강의암", 35.18923485, 128.08219657, getZone(savedZones, 11)),
                createNode(177L, "천수교 북단 3부교 가는길1", 35.18626176, 128.07497714, null),
                createNode(178L, "촉석루 앞 갈림길", 35.18970964, 128.08201622, getZone(savedZones, 1)),
                createNode(179L, "진주쌍충사적비 앞 갈림길", 35.18951927, 128.08112988, getZone(savedZones, 1)),
                createNode(180L, "옛우물", 35.18957969, 128.08067231, getZone(savedZones, 31)),
                createNode(181L, "진주성 동측 갈림길", 35.18961939, 128.08026386, null),
                createNode(182L, "공북문 앞 갈림길", 35.19003051, 128.08014854, getZone(savedZones, 3)),
                createNode(183L, "김시민 장군 전공비", 35.18986707, 128.08003109, getZone(savedZones, 32)),
                createNode(184L, "진주성 동측 위", 35.18971430, 128.08023218, null),
                createNode(185L, "영남포정사문루", 35.18961059, 128.07922897, getZone(savedZones, 2)),
                createNode(186L, "강쪽 갈림길1", 35.18909995, 128.08039697, getZone(savedZones, 4)),
                createNode(187L, "강쪽 갈림길2", 35.18890367, 128.07991953, getZone(savedZones, 4)),
                createNode(188L, "영남포정사문루 앞 갈림길", 35.18937721, 128.07937410, getZone(savedZones, 2)),
                createNode(189L, "진주경절사 앞 갈림길", 35.18958239, 128.07860005, null),
                createNode(190L, "진주경절사 (중심)", 35.18989744, 128.07839287, null),
                createNode(191L, "진주경절사 서측 갈림길", 35.18966618, 128.07779420, null),
                createNode(192L, "삼층석탑 앞 갈림길", 35.18961424, 128.07704693, null),
                createNode(193L, "김해김씨비각 앞 갈림길", 35.18904280, 128.07593326, getZone(savedZones, 33)),
                createNode(194L, "강쪽 갈림길3", 35.18875475, 128.07943723, getZone(savedZones, 5)),
                createNode(195L, "전시동 앞 갈림길1", 35.18933288, 128.07777333, getZone(savedZones, 5)),
                createNode(196L, "국립진주박물관 앞 잔디광장", 35.18899861, 128.07938008, getZone(savedZones, 5)),
                createNode(197L, "전시동 앞 갈림길2", 35.18901504, 128.07778834, getZone(savedZones, 5)),
                createNode(198L, "전시동 앞 갈림길2/강쪽갈림길2 사이 갈림길", 35.18874185, 128.07810312, getZone(savedZones, 6)),
                createNode(199L, "별관 앞", 35.18865068, 128.07745965, getZone(savedZones, 6)),
                createNode(200L, "윤선정선정비 앞 갈림길", 35.18785868, 128.07733117, null),
                createNode(201L, "진주운석빵커피", 35.18822921, 128.07696553, getZone(savedZones, 6)),
                createNode(202L, "창렬사 가는길 강변쪽 갈림길", 35.18759813, 128.07673214, getZone(savedZones, 10)),
                createNode(203L, "창렬사 (중심)", 35.18800751, 128.07605136, getZone(savedZones, 10)),
                createNode(204L, "창렬사 코너", 35.18829321, 128.07635704, getZone(savedZones, 10)),
                createNode(205L, "창렬사 코너와 김해김씨비각 사이 갈림길", 35.18887267, 128.07580751, getZone(savedZones, 10)),
                createNode(206L, "서문과 서장대 가는 갈림길", 35.18740742, 128.07638653, getZone(savedZones, 9)),
                createNode(207L, "서장대 (중심)", 35.18711356, 128.07547691, getZone(savedZones, 9)),
                createNode(208L, "서문가는 코너", 35.18731698, 128.07566074, getZone(savedZones, 9)),
                createNode(209L, "서문", 35.18763839, 128.07549755, getZone(savedZones, 9)),
                createNode(210L, "김해김씨비각", 35.18910741, 128.07550868, getZone(savedZones, 33)),
                createNode(211L, "임진대첩계사순의단(북쪽) 갈림길", 35.19025683, 128.08180389, getZone(savedZones, 1)),
                createNode(212L, "삼각형1", 35.19036091, 128.08150335, null),
                createNode(213L, "삼각형2", 35.19023572, 128.08139189, null),
                createNode(214L, "삼각형3", 35.19032716, 128.08124489, null),
                createNode(215L, "삼각형3과 공북문 사이 갈림길", 35.19008867, 128.08070101, getZone(savedZones, 3)),
                createNode(216L, "다 갈림길", 35.19003305, 128.07986311, getZone(savedZones, 3)),
                createNode(217L, "공북문 서측길", 35.19025211, 128.07980837, getZone(savedZones, 3)),
                createNode(218L, "다 갈림길과 공북문 서측길 이어지는 지점", 35.19014790, 128.07936508, null),
                createNode(219L, "성 외측 코너", 35.19023841, 128.07906984, null),
                createNode(220L, "진주경절사 뒤 갈림길", 35.19004531, 128.07874066, null),
                createNode(221L, "북장대 (중심)", 35.19008666, 128.07814559, getZone(savedZones, 8)),
                createNode(222L, "북장대 서측 갈림길1", 35.19004769, 128.07771415, getZone(savedZones, 8)),
                createNode(223L, "북장대 서측 갈림길2", 35.18983050, 128.07680001, getZone(savedZones, 7)),
                createNode(224L, "사 앞 코너", 35.18936988, 128.07589366, getZone(savedZones, 7))
        ));

        Map<Long, RouteNode> nodeMap = savedNodes.stream()
                .collect(Collectors.toMap(RouteNode::getNodeId, Function.identity()));

        // 5. Route Edges (노드 간 양방향 연결 구간 - 총 142개 간선, Haversine 거리 자동 계산)
        routeEdgeRepository.saveAll(List.of(
                // [2부교 및 망경동 남강 둔치 축]
                createEdge(nodeMap, 101L, 102L),
                createEdge(nodeMap, 102L, 103L),
                createEdge(nodeMap, 103L, 104L),
                createEdge(nodeMap, 104L, 105L),
                createEdge(nodeMap, 105L, 106L),
                createEdge(nodeMap, 106L, 107L),
                createEdge(nodeMap, 107L, 108L),
                createEdge(nodeMap, 107L, 109L),
                createEdge(nodeMap, 108L, 110L),
                createEdge(nodeMap, 109L, 111L),
                createEdge(nodeMap, 109L, 145L),
                createEdge(nodeMap, 110L, 117L),
                createEdge(nodeMap, 111L, 112L),
                createEdge(nodeMap, 112L, 118L),
                createEdge(nodeMap, 112L, 113L),
                createEdge(nodeMap, 113L, 114L),
                createEdge(nodeMap, 114L, 115L),
                createEdge(nodeMap, 115L, 116L),
                createEdge(nodeMap, 117L, 161L),
                createEdge(nodeMap, 161L, 119L),
                createEdge(nodeMap, 118L, 120L),
                createEdge(nodeMap, 119L, 126L),
                createEdge(nodeMap, 120L, 121L),
                createEdge(nodeMap, 121L, 122L),
                createEdge(nodeMap, 121L, 125L),
                createEdge(nodeMap, 122L, 123L),
                createEdge(nodeMap, 123L, 124L),
                createEdge(nodeMap, 124L, 115L),
                createEdge(nodeMap, 124L, 133L),
                createEdge(nodeMap, 126L, 125L),
                createEdge(nodeMap, 125L, 127L),

                // [진주교 및 4부교(별빛윤슬다리) 연결축]
                createEdge(nodeMap, 127L, 128L),
                createEdge(nodeMap, 127L, 142L),
                createEdge(nodeMap, 128L, 129L),
                createEdge(nodeMap, 129L, 130L),
                createEdge(nodeMap, 130L, 131L),
                createEdge(nodeMap, 131L, 132L),
                createEdge(nodeMap, 132L, 136L),
                createEdge(nodeMap, 136L, 137L),
                createEdge(nodeMap, 137L, 138L),
                createEdge(nodeMap, 133L, 134L),
                createEdge(nodeMap, 134L, 135L),
                createEdge(nodeMap, 135L, 139L),
                createEdge(nodeMap, 139L, 140L),
                createEdge(nodeMap, 140L, 141L),
                createEdge(nodeMap, 142L, 143L),
                createEdge(nodeMap, 143L, 144L),
                createEdge(nodeMap, 144L, 138L),

                // [천수교 및 유등공원 연결축]
                createEdge(nodeMap, 145L, 146L),
                createEdge(nodeMap, 146L, 147L),
                createEdge(nodeMap, 146L, 150L),
                createEdge(nodeMap, 147L, 148L),
                createEdge(nodeMap, 148L, 149L),
                createEdge(nodeMap, 150L, 151L),
                createEdge(nodeMap, 151L, 152L),
                createEdge(nodeMap, 152L, 158L),
                createEdge(nodeMap, 152L, 153L),
                createEdge(nodeMap, 153L, 154L),
                createEdge(nodeMap, 154L, 155L),
                createEdge(nodeMap, 155L, 156L),
                createEdge(nodeMap, 156L, 157L),
                createEdge(nodeMap, 158L, 159L),
                createEdge(nodeMap, 159L, 160L),

                // [배다리(3부교) 및 진주대첩광장 연결축]
                createEdge(nodeMap, 161L, 162L),
                createEdge(nodeMap, 162L, 163L),
                createEdge(nodeMap, 163L, 141L),
                createEdge(nodeMap, 163L, 165L),
                createEdge(nodeMap, 141L, 164L),

                // [신안둔치 및 1부교(용다리전설다리) 연결축]
                createEdge(nodeMap, 101L, 166L),
                createEdge(nodeMap, 166L, 167L),
                createEdge(nodeMap, 167L, 160L),
                createEdge(nodeMap, 166L, 168L),
                createEdge(nodeMap, 168L, 169L),
                createEdge(nodeMap, 169L, 170L),
                createEdge(nodeMap, 170L, 171L),
                createEdge(nodeMap, 170L, 177L),
                createEdge(nodeMap, 177L, 149L),
                createEdge(nodeMap, 171L, 172L),
                createEdge(nodeMap, 172L, 173L),
                createEdge(nodeMap, 173L, 174L),
                createEdge(nodeMap, 174L, 175L),
                createEdge(nodeMap, 175L, 176L),

                // [진주성 내부 도로망]
                createEdge(nodeMap, 164L, 178L),
                createEdge(nodeMap, 178L, 175L),
                createEdge(nodeMap, 178L, 211L),
                createEdge(nodeMap, 175L, 179L),
                createEdge(nodeMap, 179L, 180L),
                createEdge(nodeMap, 180L, 181L),
                createEdge(nodeMap, 179L, 182L),
                createEdge(nodeMap, 182L, 183L),
                createEdge(nodeMap, 182L, 184L),
                createEdge(nodeMap, 184L, 181L),
                createEdge(nodeMap, 184L, 185L),
                createEdge(nodeMap, 179L, 186L),
                createEdge(nodeMap, 181L, 186L),
                createEdge(nodeMap, 186L, 187L),
                createEdge(nodeMap, 187L, 188L),
                createEdge(nodeMap, 188L, 185L),
                createEdge(nodeMap, 185L, 189L),
                createEdge(nodeMap, 189L, 190L),
                createEdge(nodeMap, 190L, 191L),
                createEdge(nodeMap, 191L, 192L),
                createEdge(nodeMap, 192L, 193L),
                createEdge(nodeMap, 187L, 194L),
                createEdge(nodeMap, 194L, 195L),
                createEdge(nodeMap, 195L, 196L),
                createEdge(nodeMap, 194L, 198L),
                createEdge(nodeMap, 198L, 197L),
                createEdge(nodeMap, 197L, 196L),
                createEdge(nodeMap, 198L, 199L),
                createEdge(nodeMap, 199L, 200L),
                createEdge(nodeMap, 200L, 201L),
                createEdge(nodeMap, 200L, 202L),
                createEdge(nodeMap, 202L, 203L),
                createEdge(nodeMap, 202L, 204L),
                createEdge(nodeMap, 204L, 205L),
                createEdge(nodeMap, 205L, 193L),
                createEdge(nodeMap, 193L, 210L),
                createEdge(nodeMap, 202L, 206L),
                createEdge(nodeMap, 206L, 207L),
                createEdge(nodeMap, 206L, 208L),
                createEdge(nodeMap, 208L, 209L),

                // [진주성 북측 둘레길 및 성곽길]
                createEdge(nodeMap, 211L, 212L),
                createEdge(nodeMap, 212L, 213L),
                createEdge(nodeMap, 213L, 215L),
                createEdge(nodeMap, 212L, 214L),
                createEdge(nodeMap, 214L, 215L),
                createEdge(nodeMap, 215L, 182L),
                createEdge(nodeMap, 182L, 216L),
                createEdge(nodeMap, 216L, 217L),
                createEdge(nodeMap, 217L, 218L),
                createEdge(nodeMap, 216L, 218L),
                createEdge(nodeMap, 218L, 219L),
                createEdge(nodeMap, 219L, 220L),
                createEdge(nodeMap, 220L, 189L),
                createEdge(nodeMap, 220L, 221L),
                createEdge(nodeMap, 221L, 222L),
                createEdge(nodeMap, 222L, 191L),
                createEdge(nodeMap, 222L, 223L),
                createEdge(nodeMap, 223L, 192L),
                createEdge(nodeMap, 223L, 224L),
                createEdge(nodeMap, 224L, 193L)
        ));

        // 6. Restrooms (화장실 10개소, festival_id = 1)
        restroomRepository.saveAll(List.of(
                createRestroom(savedFestival, "촉석문쪽 화장실", "공중화장실", "35.19024689707712", "128.08266287756723"),
                createRestroom(savedFestival, "진주성쪽 화장실", "공중화장실", "35.19044576587172", "128.0808319962827"),
                createRestroom(savedFestival, "산청범학리 삼층석탑쪽 화장실", "공중화장실", "35.19044576587172", "128.0808319962827"),
                createRestroom(savedFestival, "천수교쪽 임시 화장실", "임시화장실", "35.18465455812448", "128.0780903036464"),
                createRestroom(savedFestival, "C존 방면 임시 화장실", "임시화장실", "35.1855396055255", "128.0789034243127"),
                createRestroom(savedFestival, "진주성 카페쪽 임시 화장실", "임시화장실", "35.1880935286506", "128.07828668844556"),
                createRestroom(savedFestival, "세계음식관 옆 임시 화장실", "임시화장실", "35.1871371328319", "128.0809281575977"),
                createRestroom(savedFestival, "수상무대쪽 임시 화장실", "임시화장실", "35.18767448293133", "128.08232134615812"),
                createRestroom(savedFestival, "성지둔치쪽 임시 화장실", "임시화장실", "35.19006806442231", "128.0842661993878"),
                createRestroom(savedFestival, "H존 방면 임시 화장실", "임시화장실", "35.188544702619076", "128.0850391858195")
        ));

        // 주차장(Parking) 초기 데이터는 공공데이터포털 실시간 API를 통해 동적으로 자동 등록됩니다.
        log.info("Seed data initialization completed successfully (33 TouristZones, 124 RouteNodes, 142 RouteEdges, 10 Restrooms registered).");
    }

    private TouristZone createZone(Festival festival, String name, String lat, String lon, int radiusMeters) {
        return TouristZone.builder()
                .festival(festival)
                .zoneName(name)
                .latitude(new BigDecimal(lat))
                .longitude(new BigDecimal(lon))
                .coverageRadiusMeters(radiusMeters)
                .build();
    }

    private TouristZone getZone(List<TouristZone> zones, int oneBasedZoneId) {
        return zones.get(oneBasedZoneId - 1);
    }

    private RouteNode createNode(Long nodeId, String nodeName, double lat, double lon, TouristZone zone) {
        return RouteNode.builder()
                .nodeId(nodeId)
                .nodeName(nodeName)
                .latitude(lat)
                .longitude(lon)
                .touristZone(zone)
                .build();
    }

    private RouteEdge createEdge(Map<Long, RouteNode> nodeMap, long startNodeId, long endNodeId) {
        RouteNode startNode = nodeMap.get(startNodeId);
        RouteNode endNode = nodeMap.get(endNodeId);
        double distanceMeters = Math.round(GeoUtil.calculateDistance(
                startNode.getLatitude(), startNode.getLongitude(),
                endNode.getLatitude(), endNode.getLongitude()) * 100.0) / 100.0;

        return RouteEdge.builder()
                .startNode(startNode)
                .endNode(endNode)
                .baseDistanceMeter(distanceMeters)
                .isBidirectional(true)
                .build();
    }

    private Restroom createRestroom(Festival festival, String name, String restroomType, String lat, String lon) {
        return Restroom.builder()
                .festival(festival)
                .name(name)
                .restroomType(restroomType)
                .latitude(new BigDecimal(lat))
                .longitude(new BigDecimal(lon))
                .build();
    }
}
