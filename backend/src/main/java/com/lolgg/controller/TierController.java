package com.lolgg.controller;

import com.lolgg.service.SummonerAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tiers")
@RequiredArgsConstructor
public class TierController {

    private final SummonerAggregationService aggregationService;

    @PostMapping
    public Map<String, Object> getTiers(@RequestBody List<String> puuids) {
        Map<String, String> tiers = aggregationService.getTiers(puuids);
        String averageTier = aggregationService.calculateAverageTier(puuids);
        return Map.of(
                "tiers", tiers,
                "averageTier", averageTier != null ? averageTier : ""
        );
    }
}
