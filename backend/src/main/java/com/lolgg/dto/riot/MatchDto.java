package com.lolgg.dto.riot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchDto(
        MetadataDto metadata,
        InfoDto info
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetadataDto(
            String matchId,
            List<String> participants
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InfoDto(
            long gameCreation,
            long gameDuration,
            long gameEndTimestamp,
            String gameMode,
            String gameType,
            int queueId,
            List<MatchParticipantDto> participants
    ) {}
}
