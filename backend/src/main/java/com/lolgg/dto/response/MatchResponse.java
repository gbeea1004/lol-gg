package com.lolgg.dto.response;

import java.util.List;

public record MatchResponse(
        String matchId,
        long gameDuration,
        long gameCreation,
        String gameMode,
        int queueId,
        String averageTier,
        ParticipantInfo currentPlayer,
        List<ParticipantInfo> participants
) {
    public record ParticipantInfo(
            String puuid,
            String gameName,
            String tagLine,
            String championName,
            int champLevel,
            int kills,
            int deaths,
            int assists,
            int cs,
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
            double killParticipation,
            int earlyLaningPhaseGoldExpAdvantage,
            int laningPhaseGoldExpAdvantage,
            int laneMinionsFirst10Minutes,
            double goldPerMinute,
            String tier
    ) {}
}
