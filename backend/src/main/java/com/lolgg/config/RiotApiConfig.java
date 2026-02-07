package com.lolgg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RiotApiConfig {

    @Value("${riot.api.key}")
    private String apiKey;

    @Bean
    public RestClient asiaRestClient() {
        return buildClient("https://asia.api.riotgames.com");
    }

    @Bean
    public RestClient krRestClient() {
        return buildClient("https://kr.api.riotgames.com");
    }

    private RestClient buildClient(String baseUrl) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        return RestClient.builder()
                .uriBuilderFactory(factory)
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
    }
}
