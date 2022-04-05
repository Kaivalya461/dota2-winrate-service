package com.kv.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeroWinRate {
    private String currentSteamName;
    private double winRate;
    private int totalMatchesPlayed;
    private String gameMode;//Turbo, AllPick...
    private String dota2AccountId;
    private String steamId;
    private long steamWebApiCallCount;
    private String errorMessage;
    private String heroName;
}
