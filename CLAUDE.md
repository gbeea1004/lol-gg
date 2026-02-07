# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

### Backend (Spring Boot 3.4.2 / Java 21)
```bash
cd backend
echo "RIOT_API_KEY=your-key" > .env   # Required, loaded by bootRun task
./gradlew bootRun                      # http://localhost:8080
./gradlew build -x test               # Build JAR (no tests exist yet)
```

### Frontend (Vue 3 / Vite / Tailwind CSS v4)
```bash
cd frontend
npm install
npm run dev       # http://localhost:5173
npm run build     # Production build to dist/
```

Both servers must run simultaneously. Frontend proxies `/api/*` to backend via Vite config.

## Architecture

**Backend** (`backend/src/main/java/com/lolgg/`):
- `config/RiotApiConfig.java` — Two RestClient beans: `asiaRestClient` (Account-V1, Match-V5) and `krRestClient` (Summoner-V4, League-V4). Uses `DefaultUriBuilderFactory` with `VALUES_ONLY` encoding mode.
- `service/SummonerAggregationService.java` — Main orchestrator. Aggregates summoner info, match history, and tier data. Contains in-memory tier cache (`ConcurrentHashMap`) with lazy loading via `POST /api/tiers`.
- `service/Riot*Service.java` — Individual Riot API wrappers (Account, Summoner, League, Match).
- DTOs are Java records with `@JsonIgnoreProperties(ignoreUnknown = true)`.
- API key is read from `RIOT_API_KEY` env var (`application.yml` references `${RIOT_API_KEY}`). The `bootRun` Gradle task auto-loads `backend/.env`.

**Frontend** (`frontend/src/`):
- `api/index.js` — Axios instance with `searchSummoner`, `getMatches(puuid, start, count, filter)`, `getTiers`.
- `views/SummonerView.vue` — Main page layout (sidebar profile/rank + match list).
- `components/MatchList.vue` — Loads matches with pagination (20 per page), game mode tab filtering (queue ID or type param), `fetchId` counter to prevent race conditions on tab switch.
- `components/MatchCard.vue` — Expandable match card. Receives `tierMap` prop from parent. Computes average tier client-side.

## API Endpoints
- `GET /api/summoner?gameName={name}&tagLine={tag}` — Summoner profile + rank
- `GET /api/matches/{puuid}?start=0&count=20&queue=420` — Match history with optional `queue` (int) or `type` (string: "ranked"/"normal") filter
- `POST /api/tiers` — Body: `["puuid1", ...]` → `{ tiers: {puuid: "Gold 3"}, averageTier: "Gold 3" }`

## Key Constraints
- **Riot API rate limit** (dev key): 20 req/s, 100 req/2min. Tier fetching uses 50ms intervals with 3-retry exponential backoff. Never prefetch tiers inline with match loading.
- **League-V4** uses `by-puuid/{puuid}` endpoint (not `by-summoner`).
- **Master+ tiers** (Master/Grandmaster/Challenger) have no rank subdivisions — display as just "Master", not "Master 1".
- **Match-V5 `challenges` field** can be null — always null-check before accessing.
- **Match filtering**: Use `queue` param for specific queues (420=솔로랭크, 440=자유랭크), `type=normal` for all normal game modes (covers queue 400/480/490).
- CORS allows GET and POST from `localhost:5173`.

## External Resources
- Data Dragon CDN: `https://ddragon.leagueoflegends.com/cdn/15.3.1` (champions, items, profile icons)
- Community Dragon: ranked emblems at `https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-static-assets/global/default/images/ranked-emblem/emblem-{tier}.png`

## UI Notes
- All user-facing text is in Korean.
- Dark theme with op.gg-inspired colors defined in `frontend/src/style.css` via Tailwind `@theme`.
- Win/lose color coding: win=#5383e8, lose=#e84057.
- Tier colors are per-tier (Iron through Challenger) defined in `MatchCard.vue`.

## Git Commit Convention
커밋 메시지는 한글로 작성. 타이틀 prefix:
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `refactor`: 리팩토링
- `design`: CSS 등 사용자 UI 디자인 변경
- `docs`: 문서 추가/수정/삭제
- `build`: 빌드 관련 파일 수정, 외부 라이브러리 추가
- `chore`: 기타 변경사항
- `style`: 코드 형식 변경 (비즈니스 로직 변경 없음)
- `test`: 테스트 코드
- `perf`: 성능 향상
- `rename`: 파일/폴더 이동 또는 이름 변경
- `remove`: 파일 삭제
- `comment`: 주석 추가/변경
- `ci`: CI 관련 설정 수정
