# 진주시 유등축제 백엔드 시스템 Quickstart 가이드

본 문서는 **진주시 유등축제 실시간 주차장 안내 및 최적 경로 추천 시스템** 백엔드 애플리케이션을 로컬 개발 환경에서 실행하고 검증 및 시연하는 방법을 안내합니다.

---

## 1. 사전 요구사항 (Prerequisites)

- **Java JDK**: 21 이상 (설치 확인: `java -version`)
- **Gradle**: 별도 설치 불필요 (프로젝트에 포함된 Gradle Wrapper `gradlew` 사용)
- **OS**: Windows / macOS / Linux 지원

---

## 2. 프로젝트 빌드 및 테스트

터미널(PowerShell 또는 명령 프롬프트)을 열고 프로젝트 루트(`backend`) 디렉토리로 이동한 후 실행합니다.

### 1) 단위 및 통합 테스트 실행 (전체 자동 검증)
```powershell
.\gradlew test
```
> Linux / macOS 환경에서는 `./gradlew test`를 실행합니다.

### 2) 프로젝트 빌드 (JAR 생성)
```powershell
.\gradlew build
```
빌드가 완료되면 `build/libs/festival-0.0.1-SNAPSHOT.jar` 파일이 생성됩니다.

---

## 3. 애플리케이션 실행 방법

### 방법 1: Gradle Wrapper를 통한 실행 (개발 환경 - H2, 기본 프로파일 `dev`)
```powershell
.\gradlew bootRun
```

### 방법 2: 빌드된 JAR 파일 직접 실행
```powershell
# 개발 환경 (H2 인메모리 DB, 기본값 dev)
java -jar build/libs/festival-0.0.1-SNAPSHOT.jar

# 운영 환경 (PostgreSQL)
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:postgresql://<host>:5432/<database>"
$env:DB_USERNAME="<username>"
$env:DB_PASSWORD="<password>"
java -jar build/libs/festival-0.0.1-SNAPSHOT.jar
```

> **프로파일별 데이터베이스 구성**
> - `dev` (`application-dev.yml`, 기본값): H2 인메모리 DB (PostgreSQL 호환 모드), H2 Console 활성화(로컬 전용), Swagger UI 활성화
> - `prod` (`application-prod.yml`): PostgreSQL DB, H2 Console 비활성화, `ddl-auto=validate`(기본값, 최초 테이블 생성 시 `JPA_DDL_AUTO=update` 지정 가능), Swagger UI 비활성화(필요 시 `SWAGGER_ENABLED=true`)

---

## 4. 접속 확인 및 대화형 개발 도구

애플리케이션이 실행(`Started FestivalApplication in ... seconds`)된 후 웹 브라우저에서 아래 도구로 접속할 수 있습니다.

- **Swagger UI (대화형 API 문서 및 테스트)**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) 또는 [http://localhost:8080/swagger](http://localhost:8080/swagger)
  - 웹 브라우저에서 `/swagger`, `/swagger-ui`, `/docs` 또는 루트 URL(`http://localhost:8080`)로 접속해도 Swagger UI 대화형 화면으로 자동 연결됩니다.
  - 모든 REST API(주차장, 관광 구역 혼잡도, AI 혼잡도 수신, A* 최적 경로 추천, 화장실 위치 안내 등)를 웹 브라우저에서 직접 파라미터를 입력하고 호출해 볼 수 있습니다.
- **OpenAPI 3.0 명세 원본 (JSON 데이터 전용)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
  - 백엔드가 제공하는 OpenAPI 3.0 스펙 raw JSON 데이터입니다. (시각적 UI를 보시려면 위의 Swagger UI 링크를 이용하세요.)
- **H2 DB Console (웹 GUI)**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **Driver Class**: `org.h2.Driver`
  - **JDBC URL**: `jdbc:h2:mem:jinjudb`
  - **User Name**: `sa`
  - **Password**: *(비워둠)*

---

## 5. 초기 시드 데이터 및 실시간 데이터 연동 구조

### 1) 초기 시드 데이터 구성 (`DataInitializer.java`)
애플리케이션 기동 시 [DataInitializer.java](src/main/java/com/jinju/festival/global/init/DataInitializer.java)를 통해 실제 진주남강유등축제 지리 데이터가 인메모리 DB에 자동 초기화됩니다.

| 테이블명 | 초기 데이터 건수 | 주요 내용 |
| :--- | :---: | :--- |
| `festivals` | 1건 | 진주남강유등축제 기본 정보 (`festival_id = 1`) |
| `tourist_zones` | 33건 | 진주성 내부, 남강 둔치, 1~4부교, 진주교/천수교 보행로 등 **33개 구역** (`zone_id`: `1`~`33`, 구역별 반경 `coverage_radius_meters` 포함) |
| `route_nodes` | 124건 | 축제장 전역 보행 도로망 **124개 노드** (`node_id`: `101`~`224`, 소속 구역 매핑 및 일반 교차로 `NULL` 포함) |
| `route_edges` | 142건 | 보행 노드 간 **142개 양방향 간선** (`GeoUtil.calculateDistance` 하버사인 공식으로 두 노드 간 실제 거리 `base_distance_meter` 자동 계산) |
| `restrooms` | 10건 | 축제장 내 상설 화장실(3개소) 및 임시 화장실(7개소) 이름 및 위경도 좌표 |
| `parkings` | 실시간 동기화 | 공공데이터포털 진주시 주차장 API 및 카카오맵 좌표 변환을 통해 스케줄러가 자동 수집/갱신 |

### 2) 외부 실시간 데이터 연동 방식 및 주기 (주차장 vs AI CCTV 혼잡도)

| 구분 | 연동 방식 | 주기 및 동작 주체 | 상세 동작 설명 |
| :--- | :---: | :--- | :--- |
| **실시간 주차장 현황** | **Pull 방식**<br>(백엔드 $\rightarrow$ 공공데이터포털) | **5분 주기 (`300,000ms`)**<br>백엔드 스케줄러 자동 호출 | 백엔드의 `DataPortalParkingScheduler`가 5분마다 공공데이터포털 API를 호출하여 주차장 잔여면수 및 혼잡도를 갱신합니다. (`application.yml`의 `api.data-portal.sync-fixed-rate`로 설정) |
| **AI CCTV 구역 혼잡도** | **Push 방식**<br>(AI 서버 $\rightarrow$ 백엔드) | **실시간 즉시 반영**<br>AI 서버가 분석 완료 시 호출 | 백엔드가 주기적으로 요청해서 가져오는 방식이 아니라, **AI 서버가 CCTV 영상 분석을 마칠 때마다 `POST /api/v1/zones/congestion` API로 결과를 전송(Push)**하면 백엔드가 즉시 `zone_congestion_history` 테이블에 저장하고 A* 경로 추천 가중치에 바로 반영합니다. |

---

## 6. 주요 REST API 목록 및 테스트 (`api-requests.http`)

프로젝트 루트에 위치한 [api-requests.http](api-requests.http) 파일 또는 Swagger UI를 통해 모든 API 엔드포인트를 호출하고 응답 JSON을 검증할 수 있습니다.

| 번호 | API 엔드포인트 | Method | 설명 |
| :---: | :--- | :---: | :--- |
| 1 | `/api/v1/festivals` | GET | 전체 축제 기본 정보 목록 조회 |
| 2 | `/api/v1/festivals/{festivalId}` | GET | 특정 축제 상세 정보 조회 |
| 3 | `/api/v1/parkings` | GET | 축제 전체 주차장 및 실시간 잔여면수/점유율 조회 |
| 4 | `/api/v1/parkings/{parkingId}` | GET | 특정 주차장 상세 및 실시간 수집 이력 조회 |
| 5 | `/api/v1/zones` | GET | 33개 관광 구역 목록 및 실시간 CCTV 인원수/혼잡도/반경 조회 |
| 6 | `/api/v1/zones/{zoneId}` | GET | 특정 관광 구역 상세 및 최근 혼잡도 이력 조회 |
| 7 | `/api/v1/zones/congestion` | POST | AI 서버 CCTV 영상 분석 결과 수신(Push) 및 혼잡도 이력(영상당 1건) 저장 |
| 8 | `/api/v1/routes/recommend` | POST / GET | CCTV 혼잡도 가중치 기반 A* 최적 경로 추천 (출발지·목적지 위경도 4개 값만으로 최근접 노드 자동 탐색 지원) |
| 9 | `/api/v1/routes/nodes` | GET | 도로망 전체 노드(`101`~`224`번, 총 124개) 좌표 및 소속 구역 목록 조회 |
| 10 | `/api/v1/restrooms` | GET | 축제장 내 전체 화장실(10개소) 이름, 유형(상설/임시) 및 위경도 좌표 조회 |
| 11 | `/api/v1/restrooms/{restroomId}` | GET | 특정 화장실 상세 위치 및 정보 조회 |

### A* 최적 경로 추천 (`POST /api/v1/routes/recommend`) 요청 예시
출발지 위·경도와 목적지 위·경도 4개 값만 전달하면, 등록된 124개 도로망 노드 중 출발지/목적지에서 가장 가까운 노드를 각각 자동 매핑하여 A* 최적 경로를 반환합니다.
```json
{
  "originLatitude": 35.1896,
  "originLongitude": 128.0807,
  "destinationLatitude": 35.1831,
  "destinationLongitude": 128.0725
}
```

---

## 7. 캡스톤 디자인 시연(Demonstration) 시나리오

1. **실시간 주차장 현황 확인 (5분 주기 Pull 동기화)**:
   - `GET /api/v1/parkings` 호출 $\rightarrow$ 공공데이터포털에서 실시간으로 수집된 주차장 목록과 카카오맵으로 변환된 위치 좌표, 최신 잔여면수 및 혼잡도를 확인합니다.
2. **화장실 및 편의시설 마커 표시**:
   - `GET /api/v1/restrooms` 호출 $\rightarrow$ 촉석문쪽 화장실, 진주성쪽 화장실, 천수교·수상무대 임시 화장실 등 10개 화장실의 이름과 위경도 좌표를 받아 프론트엔드 지도에 마커로 표시합니다.
3. **CCTV 구역별 혼잡도 확인 및 AI 분석 결과 연동 (실시간 Push 수신)**:
   - `GET /api/v1/zones` 호출 $\rightarrow$ 33개 관광 구역의 좌표, 커버리지 반경(`coverageRadiusMeters`), 실시간 인원수 및 혼잡도 가중치(원활 `1.0` / 보통 `1.3` / 혼잡 `1.8` / 매우 혼잡 `2.5`)를 확인합니다.
   - `POST /api/v1/zones/congestion` 호출 $\rightarrow$ AI 서버가 CCTV 영상 분석을 마칠 때마다 전송하는 구역별 인원수 및 혼잡도 결과를 즉시 수신하여 DB에 저장하고 실시간 혼잡도 상태를 갱신합니다.
4. **CCTV 혼잡도 기반 A* 최적 경로 추천**:
   - `POST /api/v1/routes/recommend` 또는 `GET /api/v1/routes/recommend` 호출 (출발지 위경도, 목적지 위경도 입력)
   - 124개 노드와 142개 양방향 간선 그래프 위에서 A* 알고리즘이 **혼잡 구역 가중치를 회피하여 쾌적한 우회 경로(`pathNodes`), 총 이동 거리(`totalDistanceMeters`), 예상 도보/차량 소요 시간을 산출**하는 것을 확인합니다.

---

## 8. 외부 API 및 DB 환경 변수 설정 (보안 권장: `.env` 파일 지원)

실제 외부 연동 및 운영 DB 연결에 필요한 값은 [`.env.example`](.env.example)을 복사하여 프로젝트 루트에 `.env` 파일을 생성하거나 시스템 환경 변수로 주입할 수 있습니다. 외부 API 키를 설정하지 않을 경우 기본 샘플 키로 안전하게 시뮬레이션 동작합니다.
*(주의: `.env` 파일은 API 키 및 DB 비밀번호 보안을 위해 `.gitignore`에 등록되어 Git에 커밋되지 않습니다.)*

| 환경 변수명 | 설명 | 기본값 |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | 실행 프로파일 (`dev`: H2, `prod`: PostgreSQL) | `dev` |
| `DATA_PORTAL_SERVICE_KEY` | 공공데이터포털 주차장 API 서비스 키 | `sample_service_key` |
| `KAKAO_REST_API_KEY` | 카카오 로컬/지도 REST API 키 | `sample_kakao_rest_api_key` |
| `DATA_PORTAL_SYNC_ENABLED` | 공공데이터 실시간 주차장 수집 활성화 | `true` |
| `DB_URL` | (`prod` 전용) PostgreSQL JDBC URL | *(필수)* |
| `DB_USERNAME` | (`prod` 전용) PostgreSQL 사용자명 | *(필수)* |
| `DB_PASSWORD` | (`prod` 전용) PostgreSQL 비밀번호 | *(필수)* |
| `JPA_DDL_AUTO` | (`prod` 전용) Hibernate DDL 전략 | `validate` |
| `SWAGGER_ENABLED` | (`prod` 전용) Swagger UI / OpenAPI 활성화 여부 | `false` |

**방법 1: `.env` 파일에서 간편하게 설정 (가장 추천)**
프로젝트 루트의 `.env` 파일을 열고 발급받은 키를 입력한 뒤 실행하면 자동으로 적용됩니다:
```env
KAKAO_REST_API_KEY=발급받은_실제_카카오_REST_API키
DATA_PORTAL_SERVICE_KEY=발급받은_실제_공공데이터_서비스키
```

**방법 2: PowerShell 환경 변수 직접 설정 후 실행**
```powershell
$env:DATA_PORTAL_SERVICE_KEY="실제발급받은키"
$env:KAKAO_REST_API_KEY="실제발급받은키"
.\gradlew bootRun
```

---

## 9. 주요 파일 및 디렉토리 안내

- **요구사항 및 상세 명세**: [PROJECT_SPEC.md](spec/PROJECT_SPEC.md)
- **API 테스트 명세 파일**: [api-requests.http](api-requests.http)
- **초기 시드 데이터 설정**: [DataInitializer.java](src/main/java/com/jinju/festival/global/init/DataInitializer.java)
- **애플리케이션 설정 파일**: [application.yml](src/main/resources/application.yml)
- **메인 애플리케이션 클래스**: [FestivalApplication.java](src/main/java/com/jinju/festival/FestivalApplication.java)
- **빌드 설정 파일**: [build.gradle](build.gradle)
