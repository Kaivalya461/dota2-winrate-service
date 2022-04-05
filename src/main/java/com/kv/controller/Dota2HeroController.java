package com.kv.controller;

import com.kv.service.Dota2HeroService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dota2-hero")
public class Dota2HeroController {

    @Autowired
    private Dota2HeroService dota2HeroService;

    @GetMapping("hero-name-list")
    public ResponseEntity<List<String>> getAllHeroNamesList() {
        return ResponseEntity.ok(dota2HeroService.getAllHeroNamesList());
    }

    @GetMapping("/hero-name/{heroId}")
    public ResponseEntity<String> getHeroList(@PathVariable @NonNull String heroId) {
        return dota2HeroService.getHeroName(heroId);
    }
}
