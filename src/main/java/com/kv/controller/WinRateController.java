package com.kv.controller;

import com.kv.winrate.dto.HeroWinRate;
import com.kv.service.WinRateService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Controller
@RequestMapping(value = "/win-rate")
@CrossOrigin(value = "*")
public class WinRateController {

    @Autowired
    private WinRateService winRateService;

    //ToDo implement db for saving all steam api responses
    @GetMapping(value = "/in-dev/{dota2AccountId}")
    public ResponseEntity<HeroWinRate> getWinRate(@PathVariable @NonNull String dota2AccountId,
                                                  @RequestParam String heroId,
                                                  @RequestParam String gameMode) {
        return ResponseEntity.ok(winRateService.getWinRate(dota2AccountId, heroId, gameMode));
    }

    @GetMapping(value = "/getWinRateOfTodaysPlayedMatches/{dota2AccountId}")
    public ResponseEntity<String> getWinRateOfTodaysPlayedMatches(@PathVariable String dota2AccountId) {
        return winRateService.getWinRateOfTodaysPlayedMatches(dota2AccountId);
    }

    @GetMapping(value = "/highestWinRateHeroes/{dota2AccountId}")
    public ResponseEntity<Collection<HeroWinRate>> getHighestWinRateHeroes(@PathVariable String dota2AccountId) {
//        return winRateService.getWinRateOfTodaysPlayedMatches(dota2AccountId);
        return ResponseEntity.ok(winRateService.getHighestWinRateHeroesForLastMonth(dota2AccountId));
    }
}
