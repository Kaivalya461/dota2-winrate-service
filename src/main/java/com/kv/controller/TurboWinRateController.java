package com.kv.controller;

import com.kv.model.HeroWinRate;
import com.kv.service.TurboWinRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class TurboWinRateController {

    @Autowired
    private TurboWinRateService turboWinRateService;

    //In-progress
    @GetMapping(value = "/getWinRate/{dota2AccountId}")
    public HeroWinRate getWinRate(@PathVariable String dota2AccountId) throws IOException {
        return turboWinRateService.getTurboModeWinRateForLast500Matches(dota2AccountId);
    }
}
