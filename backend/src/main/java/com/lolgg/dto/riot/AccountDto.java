package com.lolgg.dto.riot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountDto(
        String puuid,
        String gameName,
        String tagLine
) {}
