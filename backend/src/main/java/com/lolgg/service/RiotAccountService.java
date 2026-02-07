package com.lolgg.service;

import com.lolgg.dto.riot.AccountDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RiotAccountService {

    private final RestClient asiaRestClient;

    public RiotAccountService(@Qualifier("asiaRestClient") RestClient asiaRestClient) {
        this.asiaRestClient = asiaRestClient;
    }

    public AccountDto getAccountByRiotId(String gameName, String tagLine) {
        return asiaRestClient.get()
                .uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine)
                .retrieve()
                .body(AccountDto.class);
    }
}
