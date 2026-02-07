package com.lolgg.dto.riot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LeagueEntryDto(
        String leagueId,
        String summonerId,
        String queueType,
        String tier,
        String rank,
        int leaguePoints,
        int wins,
        int losses,
        boolean hotStreak,
        boolean veteran,
        boolean freshBlood,
        boolean inactive
) {}
