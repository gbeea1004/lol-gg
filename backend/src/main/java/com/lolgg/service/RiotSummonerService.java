package com.lolgg.service;

import com.lolgg.dto.riot.SummonerDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RiotSummonerService {

    private final RestClient krRestClient;

    public RiotSummonerService(@Qualifier("krRestClient") RestClient krRestClient) {
        this.krRestClient = krRestClient;
    }

    public SummonerDto getSummonerByPuuid(String puuid) {
        return krRestClient.get()
                .uri("/lol/summoner/v4/summoners/by-puuid/{puuid}", puuid)
                .retrieve()
                .body(SummonerDto.class);
    }
}
