package com.lolgg.service;

import com.lolgg.dto.response.MatchResponse;
import com.lolgg.dto.response.SummonerResponse;
import com.lolgg.dto.riot.AccountDto;
import com.lolgg.dto.riot.LeagueEntryDto;
import com.lolgg.dto.riot.MatchDto;
import com.lolgg.dto.riot.MatchParticipantDto;
import com.lolgg.dto.riot.SummonerDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SummonerAggregationService {

    private static final Logger log = LoggerFactory.getLogger(SummonerAggregationService.class);

    private final RiotAccountService accountService;
    private final RiotSummonerService summonerService;
    private final RiotLeagueService leagueService;
    private final RiotMatchService matchService;

    private final Map<String, String> tierCache = new ConcurrentHashMap<>();

    private static final Map<String, Integer> TIER_VALUES = Map.ofEntries(
            Map.entry("IRON", 1), Map.entry("BRONZE", 2), Map.entry("SILVER", 3),
            Map.entry("GOLD", 4), Map.entry("PLATINUM", 5), Map.entry("EMERALD", 6),
            Map.entry("DIAMOND", 7), Map.entry("MASTER", 8),
            Map.entry("GRANDMASTER", 9), Map.entry("CHALLENGER", 10)
    );

    private static final Map<String, Integer> RANK_VALUES = Map.of(
            "IV", 0, "III", 1, "II", 2, "I", 3
    );

    public SummonerResponse getSummonerInfo(String gameName, String tagLine) {
        AccountDto account = accountService.getAccountByRiotId(gameName, tagLine);
        SummonerDto summoner = summonerService.getSummonerByPuuid(account.puuid());
        List<LeagueEntryDto> leagues = leagueService.getLeagueEntries(account.puuid());

        return new SummonerResponse(
                account.puuid(),
                account.gameName(),
                account.tagLine(),
                summoner.profileIconId(),
                summoner.summonerLevel(),
                leagues
        );
    }

    public List<MatchResponse> getMatches(String puuid, int start, int count, Integer queue, String type) {
        List<String> matchIds = matchService.getMatchIds(puuid, start, count, queue, type);

        List<MatchDto> matches = matchIds.stream()
                .map(matchService::getMatch)
                .toList();

        return matches.stream()
                .map(match -> toMatchResponse(match, puuid))
                .toList();
    }

    /**
     * Lazily fetch tiers for a list of puuids (called when match card is expanded).
     * Uses conservative rate limiting to avoid 429 errors.
     */
    public Map<String, String> getTiers(List<String> puuids) {
        Map<String, String> result = new HashMap<>();

        for (int i = 0; i < puuids.size(); i++) {
            String puuid = puuids.get(i);

            // Return cached value if available
            if (tierCache.containsKey(puuid)) {
                result.put(puuid, formatTier(tierCache.get(puuid)));
                continue;
            }

            // Rate limit: sleep 50ms between API calls (~10 req/s within 20/s limit)
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}

            int retries = 0;
            while (retries < 3) {
                try {
                    List<LeagueEntryDto> entries = leagueService.getLeagueEntries(puuid);
                    String soloTier = entries.stream()
                            .filter(e -> "RANKED_SOLO_5x5".equals(e.queueType()))
                            .findFirst()
                            .map(e -> e.tier() + " " + e.rank())
                            .orElse(null);
                    tierCache.put(puuid, soloTier != null ? soloTier : "UNRANKED");
                    result.put(puuid, formatTier(tierCache.get(puuid)));
                    break;
                } catch (Exception e) {
                    retries++;
                    if (retries < 3) {
                        log.warn("Retry {}/3 for puuid: {} - {}", retries, puuid.substring(0, 8), e.getMessage());
                        try { Thread.sleep(2000L * retries); } catch (InterruptedException ignored) {}
                    } else {
                        log.warn("Failed to fetch tier for puuid after 3 retries: {}", puuid.substring(0, 8));
                        result.put(puuid, null);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Calculate average tier from the cached tiers (called after getTiers populates the cache).
     */
    public String calculateAverageTier(List<String> puuids) {
        int totalValue = 0;
        int count = 0;

        for (String puuid : puuids) {
            String cached = tierCache.get(puuid);
            if (cached == null || "UNRANKED".equals(cached)) continue;

            String[] parts = cached.split(" ");
            if (parts.length != 2) continue;

            Integer tierVal = TIER_VALUES.get(parts[0]);
            Integer rankVal = RANK_VALUES.get(parts[1]);
            if (tierVal == null || rankVal == null) continue;

            totalValue += tierVal * 4 + rankVal;
            count++;
        }

        if (count == 0) return null;

        int avg = Math.round((float) totalValue / count);
        final int finalTierIdx = Math.max(1, Math.min(avg / 4, 10));
        int rankIdx = avg % 4;

        String tier = TIER_VALUES.entrySet().stream()
                .filter(e -> e.getValue() == finalTierIdx)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("GOLD");

        if (finalTierIdx >= 8) {
            return tier.charAt(0) + tier.substring(1).toLowerCase();
        }

        String[] rankNames = {"IV", "III", "II", "I"};
        String rank = rankIdx >= 0 && rankIdx < 4 ? rankNames[rankIdx] : "IV";

        Map<String, String> rankToNum = Map.of("I", "1", "II", "2", "III", "3", "IV", "4");
        return tier.charAt(0) + tier.substring(1).toLowerCase() + " " + rankToNum.getOrDefault(rank, "4");
    }

    private MatchResponse toMatchResponse(MatchDto match, String puuid) {
        List<MatchResponse.ParticipantInfo> participants = match.info().participants().stream()
                .map(mp -> toParticipantInfo(mp, null))
                .toList();

        MatchResponse.ParticipantInfo currentPlayer = participants.stream()
                .filter(p -> p.puuid().equals(puuid))
                .findFirst()
                .orElse(null);

        return new MatchResponse(
                match.metadata().matchId(),
                match.info().gameDuration(),
                match.info().gameCreation(),
                match.info().gameMode(),
                match.info().queueId(),
                null,
                currentPlayer,
                participants
        );
    }

    private MatchResponse.ParticipantInfo toParticipantInfo(MatchParticipantDto p, String rawTier) {
        var challenges = p.challenges();
        return new MatchResponse.ParticipantInfo(
                p.puuid(),
                p.riotIdGameName(),
                p.riotIdTagline(),
                p.championName(),
                p.champLevel(),
                p.kills(),
                p.deaths(),
                p.assists(),
                p.totalMinionsKilled() + p.neutralMinionsKilled(),
                p.goldEarned(),
                p.totalDamageDealtToChampions(),
                p.visionScore(),
                p.item0(),
                p.item1(),
                p.item2(),
                p.item3(),
                p.item4(),
                p.item5(),
                p.item6(),
                p.summoner1Id(),
                p.summoner2Id(),
                p.win(),
                p.teamPosition(),
                p.teamId(),
                challenges != null ? challenges.killParticipation() : 0,
                challenges != null ? challenges.earlyLaningPhaseGoldExpAdvantage() : 0,
                challenges != null ? challenges.laningPhaseGoldExpAdvantage() : 0,
                challenges != null ? challenges.laneMinionsFirst10Minutes() : 0,
                challenges != null ? challenges.goldPerMinute() : 0,
                formatTier(rawTier)
        );
    }

    private String formatTier(String rawTier) {
        if (rawTier == null || "UNRANKED".equals(rawTier)) return null;
        String[] parts = rawTier.split(" ");
        if (parts.length != 2) return rawTier;
        String tier = parts[0].charAt(0) + parts[0].substring(1).toLowerCase();
        // Master+ tiers don't have rank subdivisions
        Integer tierVal = TIER_VALUES.get(parts[0]);
        if (tierVal != null && tierVal >= 8) {
            return tier;
        }
        Map<String, String> rankToNum = Map.of("I", "1", "II", "2", "III", "3", "IV", "4");
        return tier + " " + rankToNum.getOrDefault(parts[1], parts[1]);
    }
}
