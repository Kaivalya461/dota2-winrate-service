package com.kv.service;

import com.kv.model.dtos.HeroesDto;
import com.kv.util.Dota2HeroesUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class Dota2HeroService {

    public List<String> getAllHeroNamesList() {
        return Dota2HeroesUtil.getAllHeroes().stream().map(HeroesDto::getLocalized_name).collect(Collectors.toList());
    }

    public ResponseEntity<String> getHeroName(String heroId) {
//        HeroesDto heroDto = Dota2HeroesUtil.getAllHeroes().stream().filter(hero -> hero.getId() == Long.parseLong(heroId)).findFirst().orElseThrow(() -> new RestClientException("Hero not found"));
//        return heroDto != null ? heroDto.getLocalized_name() : null;
        ResponseEntity<String> responseEntity;
        String heroName;
        try {
            heroName = Dota2HeroesUtil.getHeroName(Integer.parseInt(heroId));
            responseEntity = new ResponseEntity<>(heroName, HttpStatus.OK);
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        return responseEntity;
    }
}
