package com.kv.controller;

import com.kv.service.MostPlayedHeroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MostPlayedHeroController {

    @Autowired
    private MostPlayedHeroService mostPlayedHeroService;

    @GetMapping(value = "/getMostPlayedHero/{dota2AccountId}")
    public ResponseEntity<String> getMostPlayedHeroForADota2AccountId(@PathVariable String dota2AccountId) {
        return mostPlayedHeroService.getMostPlayedHeroForLast500Matches(dota2AccountId);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/getTodaysMostPlayedHero/{dota2AccountId}")
    public ResponseEntity<String> getTodaysMostPlayedHero(@PathVariable String dota2AccountId) {
        return mostPlayedHeroService.getTodaysMostPlayedHero(dota2AccountId);
    }
}
