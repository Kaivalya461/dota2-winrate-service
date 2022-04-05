package com.kv.service;

import com.kv.model.dtos.MatchDetailsDto;
import com.kv.matchdetails.dto.MatchesDto;
import com.kv.player.dto.PlayerDto;
import com.kv.util.Dota2HeroesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MostPlayedHeroService {
    @Autowired
    private SteamWebApiQueryService steamWebApiQueryService;

    public ResponseEntity<String> getMostPlayedHeroForLast500Matches(String dota2AccountId) {
        Set<MatchesDto> allMatches;
        int accountId = Integer.parseInt(dota2AccountId);

        try {
            allMatches = steamWebApiQueryService.getLast500MatchesForDota2AccountId(dota2AccountId);
        } catch(Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        Optional<Integer> mostPlayedHeroOptional = getMostPlayedHero(allMatches, accountId);    //Huge processing

        int mostPlayedHero = Objects.requireNonNull(mostPlayedHeroOptional.orElse(0));
        try {
            return ResponseEntity.ok(Dota2HeroesUtil.getHeroName(mostPlayedHero));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     *  This function takes dota 2 accound Id as input and gives the 'Most Played hero for the recent date for which Player has played Dota 2'
     *  All Matches played from 3AM of the recently played match's date are taken into processing
     */
    public ResponseEntity<String> getTodaysMostPlayedHero(String dota2AccountId) {
        Set<MatchesDto> allMatches;
        int accountId = Integer.parseInt(dota2AccountId);
        Optional<Integer> mostPlayedHeroOptional;
        int mostPlayedHeroId;

        try {
            allMatches = steamWebApiQueryService.getTodaysPlayedMatches(dota2AccountId);
        } catch(Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        if(!allMatches.isEmpty()) {
            mostPlayedHeroOptional = getMostPlayedHero(allMatches, accountId);  //Huge processing
            mostPlayedHeroId = Objects.requireNonNull(mostPlayedHeroOptional.orElse(0));
        }
        else
            return ResponseEntity.status(HttpStatus.OK).body("Zero matches found for this Dota2 ID");

        try {
            return ResponseEntity.ok(Dota2HeroesUtil.getHeroName(mostPlayedHeroId));  //return Full name of the hero based on provided HeroId
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    //In-Progress
    public ResponseEntity<Object> getWinRateOfASpecificDota2Hero(String dota2AccountId, String dota2HeroId) {



        return null;
    }

    private Optional<Integer> getMostPlayedHero(Set<MatchesDto> allMatches, Integer accountId) {
        //Heroes played for this dota2AccountID
        List<Integer> heroesPlayed = new ArrayList<>();
        allMatches.forEach(matchesDto -> {
            heroesPlayed.addAll(matchesDto.getPlayers().stream().filter(player -> player.getAccount_id() == accountId)
                    .map(PlayerDto::getHero_id).collect(Collectors.toList()));
        });

        //Map of heroes played as heroId(Map Key) and their matches played count(Map Value)
        //Calculating matches played count(Map Value) based on their occurrence in the list
        Map<Integer, Integer> heroPlayedMap = new HashMap<>();
        for (Integer hero: heroesPlayed) {
            heroPlayedMap.putIfAbsent(hero, 0);
            int heroPlayedCount = heroPlayedMap.get(hero);
            heroPlayedCount++;
            heroPlayedMap.put(hero, heroPlayedCount);
        }

        //Mapping all the matches played count(Map Value) of all heroes to a List
        List<Integer> allMatchesPlayedCount = heroPlayedMap.keySet().stream().map(heroPlayedMap::get).collect(Collectors.toList());

        //Taking max number from the matches played count list and storing it in a variable
        Comparator<Integer> c = (i1, i2) -> (i1 < i2)?-1 : (i1 > i2)?1 : 0;
        Optional<Integer> highestMatchesPlayedOnThatHeroCount = allMatchesPlayedCount.stream().max(c);

        //Iterating through all elements from the map and finding which heroId has this max number
        Optional<Integer> mostPlayedHeroOptional = getMostPlayedHeroFromTheMap(heroPlayedMap, Objects.requireNonNull(highestMatchesPlayedOnThatHeroCount.orElse(0)));

        return mostPlayedHeroOptional;
    }

    private Optional<Integer> getMostPlayedHeroFromTheMap(Map<Integer, Integer> heroPlayedMap, Integer highestMatchesPlayedOnThatHeroCount) {
        return heroPlayedMap.keySet().stream().filter(hero -> heroPlayedMap.get(hero).equals(highestMatchesPlayedOnThatHeroCount)).findFirst();
    }
}
