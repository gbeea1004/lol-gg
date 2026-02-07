package com.lolgg.dto.riot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SummonerDto(
        String id,
        String accountId,
        String puuid,
        int profileIconId,
        long revisionDate,
        long summonerLevel
) {}
