# 진주남강유등축제 길안내 및 혼잡도 안내 시스템 (Backend)

> 본 저장소는 **'진주남강유등축제 길안내 및 혼잡도 안내 시스템'** 팀 프로젝트에서 **백엔드 API 서버, 도로 네트워크 DB 스키마 설계, A\* 우회 경로 탐색 엔진**을 담당하여 구현한 소스 코드입니다.

---

## 주요 기능

- **CCTV 혼잡도 기반 A\* 우회 경로 탐색 엔진**
  - 진주성·남강 둔치·1~4부교·진주교·천수교 보행로를 모델링한 **124개 노드 / 142개 양방향 간선** 도로 네트워크 그래프 구축
  - 구역별 실시간 혼잡도 가중치(원활 `1.0` / 보통 `1.3` / 혼잡 `1.8` / 매우 혼잡 `2.5`)를 반영해 혼잡 구역을 회피하는 최적 도보 경로 추천
  - 출발지·목적지 GPS 위경도만으로 최근접 도로망 노드 자동 매핑
- **AI CCTV 구역별 실시간 혼잡도 수신 및 이력 관리**
  - 33개 주요 관광 구역(`tourist_zones`) 관리 및 AI 영상 분석 서버의 실시간 Push 데이터 수신(`POST /api/v1/zones/congestion`)
- **실시간 주차장 현황 자동 동기화 (Pull)**
  - 공공데이터포털 진주시 주차장 API를 5분 주기로 수집하고 카카오맵 지오코딩 API로 좌표를 매핑해 잔여면수·점유율·혼잡상태 갱신
- **축제 정보 및 편의시설 안내**
  - 축제 기본 정보 및 축제장 내 상설/임시 화장실(10개소) 위치 좌표 제공

---

## 기술 스택

| 구분 | 기술 |
| :--- | :--- |
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.1.1, Spring Web MVC, Spring Data JPA, Spring Validation |
| **Database** | H2 (`dev`, PostgreSQL 호환 모드) / PostgreSQL (`prod`) |
| **API Docs** | Springdoc OpenAPI 3 (Swagger UI) |
| **Build Tool** | Gradle (Wrapper 포함) |
| **External API** | 공공데이터포털 진주시 실시간 주차장 API, 카카오 로컬/모빌리티 REST API |

---

## 주요 API 엔드포인트

| Method | Endpoint | 설명 |
| :---: | :--- | :--- |
| `GET` | `/api/v1/festivals` | 전체 축제 정보 목록 조회 |
| `GET` | `/api/v1/festivals/{festivalId}` | 특정 축제 상세 정보 조회 |
| `GET` | `/api/v1/parkings` | 전체 주차장 및 실시간 잔여면수·점유율 조회 |
| `GET` | `/api/v1/parkings/{parkingId}` | 특정 주차장 상세 및 수집 이력 조회 |
| `GET` | `/api/v1/zones` | 33개 관광 구역 및 실시간 CCTV 혼잡도 조회 |
| `GET` | `/api/v1/zones/{zoneId}` | 특정 관광 구역 상세 및 혼잡도 이력 조회 |
| `POST` | `/api/v1/zones/congestion` | AI 서버 CCTV 영상 분석 결과 수신 및 저장 |
| `POST` / `GET` | `/api/v1/routes/recommend` | CCTV 혼잡도 가중치 기반 A\* 최적 우회 경로 추천 |
| `GET` | `/api/v1/routes/nodes` | 보행 도로망 전체 노드 목록 조회 |
| `GET` | `/api/v1/restrooms` | 축제장 내 전체 화장실 위치 목록 조회 |
| `GET` | `/api/v1/restrooms/{restroomId}` | 특정 화장실 상세 정보 조회 |

---

## 실행 방법

### 1. 환경 변수 설정
`.env.example`을 복사하여 `.env` 파일을 생성한 뒤 필요한 키를 입력합니다. (`.env`는 `.gitignore`에 포함되어 커밋되지 않습니다.)
```bash
cp .env.example .env
```
외부 API 키를 입력하지 않아도 로컬 개발 환경에서는 샘플 시뮬레이션 모드로 정상 실행됩니다.

### 2. 개발 환경 실행 (`dev` 프로파일 - H2, 기본값)
```powershell
# Windows (PowerShell)
.\gradlew bootRun

# macOS / Linux
./gradlew bootRun
```
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **H2 Console**: `http://localhost:8080/h2-console` (`jdbc:h2:mem:jinjudb`, username: `sa`, password 없음)

### 3. 운영 환경 실행 (`prod` 프로파일 - PostgreSQL)
```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:postgresql://<host>:5432/<database>"
$env:DB_USERNAME="<username>"
$env:DB_PASSWORD="<password>"
# 최초 빈 DB에 스키마 생성이 필요한 경우에만 지정 (기본값: validate)
# $env:JPA_DDL_AUTO="update"

java -jar build/libs/festival-0.0.1-SNAPSHOT.jar
```

### 4. 테스트 실행
```powershell
.\gradlew test
```

자세한 실행 및 API 테스트 방법은 [Quickstart.md](Quickstart.md)와 [api-requests.http](api-requests.http)를 참고하세요.

---

## 프로젝트 구조

```text
src/main/java/com/jinju/festival
├── domain
│   ├── festival        # 축제 기본 정보
│   ├── parking         # 실시간 주차장 현황 및 이력
│   ├── restroom        # 축제장 화장실 위치 안내
│   ├── route           # 도로망 노드/간선 및 A* 경로 추천
│   └── zone            # CCTV 관광 구역 및 실시간 혼잡도
├── global
│   ├── config          # Swagger, RestClient, JPA Auditing, Dotenv 설정
│   ├── error           # 전역 예외 처리
│   ├── init            # 초기 도로망·구역·화장실 시드 데이터 초기화
│   ├── response        # 공통 API 응답 래퍼
│   └── util            # A* 경로 탐색 알고리즘, Haversine 거리 계산
└── infrastructure
    ├── dataportal      # 공공데이터포털 주차장 API 연동 및 스케줄러
    └── kakao           # 카카오 로컬/모빌리티 API 연동
```
