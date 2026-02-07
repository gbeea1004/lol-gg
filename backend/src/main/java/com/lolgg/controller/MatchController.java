package com.lolgg.controller;

import com.lolgg.dto.response.MatchResponse;
import com.lolgg.service.SummonerAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final SummonerAggregationService aggregationService;

    @GetMapping("/{puuid}")
    public List<MatchResponse> getMatches(
            @PathVariable String puuid,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "20") int count,
            @RequestParam(required = false) Integer queue,
            @RequestParam(required = false) String type) {
        return aggregationService.getMatches(puuid, start, count, queue, type);
    }
}
