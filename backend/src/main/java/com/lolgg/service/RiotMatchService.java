package com.lolgg.service;

import com.lolgg.dto.riot.MatchDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class RiotMatchService {

    private final RestClient asiaRestClient;

    public RiotMatchService(@Qualifier("asiaRestClient") RestClient asiaRestClient) {
        this.asiaRestClient = asiaRestClient;
    }

    public List<String> getMatchIds(String puuid, int start, int count, Integer queue, String type) {
        if (queue != null) {
            return asiaRestClient.get()
                    .uri("/lol/match/v5/matches/by-puuid/{puuid}/ids?start={start}&count={count}&queue={queue}",
                            puuid, start, count, queue)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        }
        if (type != null) {
            return asiaRestClient.get()
                    .uri("/lol/match/v5/matches/by-puuid/{puuid}/ids?start={start}&count={count}&type={type}",
                            puuid, start, count, type)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        }
        return asiaRestClient.get()
                .uri("/lol/match/v5/matches/by-puuid/{puuid}/ids?start={start}&count={count}",
                        puuid, start, count)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public MatchDto getMatch(String matchId) {
        return asiaRestClient.get()
                .uri("/lol/match/v5/matches/{matchId}", matchId)
                .retrieve()
                .body(MatchDto.class);
    }
}
