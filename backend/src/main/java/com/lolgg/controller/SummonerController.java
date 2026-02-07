package com.lolgg.controller;

import com.lolgg.dto.response.SummonerResponse;
import com.lolgg.service.SummonerAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summoner")
@RequiredArgsConstructor
public class SummonerController {

    private final SummonerAggregationService aggregationService;

    @GetMapping
    public SummonerResponse getSummoner(
            @RequestParam String gameName,
            @RequestParam String tagLine) {
        return aggregationService.getSummonerInfo(gameName, tagLine);
    }
}
