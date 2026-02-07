package com.lolgg.dto.response;

import com.lolgg.dto.riot.LeagueEntryDto;

import java.util.List;

public record SummonerResponse(
        String puuid,
        String gameName,
        String tagLine,
        int profileIconId,
        long summonerLevel,
        List<LeagueEntryDto> leagues
) {}
