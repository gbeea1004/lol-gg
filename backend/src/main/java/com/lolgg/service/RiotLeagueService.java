package com.lolgg.service;

import com.lolgg.dto.riot.LeagueEntryDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class RiotLeagueService {

    private final RestClient krRestClient;

    public RiotLeagueService(@Qualifier("krRestClient") RestClient krRestClient) {
        this.krRestClient = krRestClient;
    }

    public List<LeagueEntryDto> getLeagueEntries(String puuid) {
        return krRestClient.get()
                .uri("/lol/league/v4/entries/by-puuid/{puuid}", puuid)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
