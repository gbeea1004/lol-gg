# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

### Backend (Spring Boot 3.4.2 / Java 21)
```bash
cd backend
./gradlew bootRun          # Dev server on http://localhost:8080
./gradlew build -x test    # Build JAR (no tests exist yet)
```

### Frontend (Vue 3 / Vite / Tailwind CSS v4)
```bash
cd frontend
npm install                # First time only
npm run dev                # Dev server on http://localhost:5173
npm run build              # Production build to dist/
```

Both servers must run simultaneously. Frontend proxies `/api/*` to backend via Vite config.

## Architecture

**Backend** (`backend/src/main/java/com/lolgg/`):
- `config/RiotApiConfig.java` — Two RestClient beans: `asiaRestClient` (Account-V1, Match-V5) and `krRestClient` (Summoner-V4, League-V4). Uses `DefaultUriBuilderFactory` with `VALUES_ONLY` encoding mode.
- `service/SummonerAggregationService.java` — Main orchestrator. Aggregates summoner info, match history, and tier data. Contains in-memory tier cache (`ConcurrentHashMap`) with lazy loading via `POST /api/tiers`.
- `service/Riot*Service.java` — Individual Riot API wrappers (Account, Summoner, League, Match).
- DTOs are Java records with `@JsonIgnoreProperties(ignoreUnknown = true)`.

**Frontend** (`frontend/src/`):
- `api/index.js` — Axios instance with three functions: `searchSummoner`, `getMatches`, `getTiers`.
- `views/SummonerView.vue` — Main page layout (sidebar profile/rank + match list).
- `components/MatchList.vue` — Loads matches with pagination (20 per page), prefetches all participant tiers in background after match load.
- `components/MatchCard.vue` — Expandable match card. Receives `tierMap` prop from parent. Computes average tier client-side from tier map.

## API Endpoints
- `GET /api/summoner?gameName={name}&tagLine={tag}` — Summoner profile + rank
- `GET /api/matches/{puuid}?start=0&count=20` — Match history (no tier data)
- `POST /api/tiers` — Body: `["puuid1", "puuid2", ...]` → `{ tiers: {puuid: "Gold 3"}, averageTier: "Gold 3" }`

## Key Constraints
- **Riot API rate limit** (dev key): 20 req/s, 100 req/2min. Tier fetching uses 50ms intervals with 3-retry exponential backoff. Never prefetch tiers inline with match loading.
- **League-V4** uses `by-puuid/{puuid}` endpoint (not `by-summoner`).
- **Master+ tiers** (Master/Grandmaster/Challenger) have no rank subdivisions — display as just "Master", not "Master 1".
- **Match-V5 `challenges` field** can be null — always null-check before accessing.
- CORS allows GET and POST from `localhost:5173`.

## External Resources
- Data Dragon CDN: `https://ddragon.leagueoflegends.com/cdn/15.3.1` (champions, items, profile icons)
- Community Dragon: ranked emblems at `https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-static-assets/global/default/images/ranked-emblem/emblem-{tier}.png`

## UI Notes
- All user-facing text is in Korean.
- Dark theme with op.gg-inspired colors defined in `frontend/src/style.css` via Tailwind `@theme`.
- Win/lose color coding: win=#5383e8, lose=#e84057.
- Tier colors are per-tier (Iron through Challenger) defined in `MatchCard.vue`.
