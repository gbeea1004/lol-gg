package com.lolgg.dto.riot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchParticipantDto(
        String puuid,
        String summonerName,
        String riotIdGameName,
        String riotIdTagline,
        String championName,
        int championId,
        int champLevel,
        int kills,
        int deaths,
        int assists,
        int totalMinionsKilled,
        int neutralMinionsKilled,
        int goldEarned,
        int totalDamageDealtToChampions,
        int visionScore,
        int item0,
        int item1,
        int item2,
        int item3,
        int item4,
        int item5,
        int item6,
        int summoner1Id,
        int summoner2Id,
        boolean win,
        String teamPosition,
        int teamId,
        ChallengesDto challenges
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChallengesDto(
            double killParticipation,
            double goldPerMinute,
            int laneMinionsFirst10Minutes,
            double maxCsAdvantageOnLaneOpponent,
            int earlyLaningPhaseGoldExpAdvantage,
            int laningPhaseGoldExpAdvantage
    ) {}
}
