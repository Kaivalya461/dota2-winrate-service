package com.kv.service;

import com.kv.aop.annotation.LogExecutionTime;
import com.kv.winrate.dto.HeroWinRate;
import com.kv.matchdetails.dto.MatchDetailsDto;
import com.kv.matchdetails.dto.MatchesDto;
import com.kv.hero.dto.HeroesDto;
import com.kv.player.dto.PlayerDto;
import com.kv.util.Dota2HeroesUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Log4j2
public class WinRateService {
    @Autowired
    private SteamWebApiQueryService steamWebApiQueryService;

    @Autowired
    private Dota2QueryService dota2QueryService;

    @Autowired private SteamWebAPICacheService steamWebAPICacheService;

    private static final int TURBO_GAME_MODE_ID = 23;
    private static final int PUBLIC_MATCHMAKING_LOBBY_TYPE_ID = 0;
    private static final short FIVE_AND_HALF_HOURS_TIME_IN_SECONDS = 19800;
    private final Range<Integer> RADIANT_PLAYER_SLOTS = Range.of(Range.Bound.inclusive(0), Range.Bound.inclusive(4));

    public HeroWinRate getWinRate(String dota2AccountId, String heroId, String gameMode) {
        HeroWinRate winRate = new HeroWinRate();
        String heroName;

        //Validate that the dota 2 hero exists for the heroid passed in query params
        try {
            heroName = Dota2HeroesUtil.getHeroName(Integer.valueOf(heroId));
        } catch (Exception e) {
            log.error("Error while fetching HeroName. Error = {}", e.getMessage());
            winRate.setErrorMessage(e.getMessage());
            return winRate;
        }

        double winRateDouble = 0;
        int gameModeInteger = Integer.parseInt(gameMode);
        Set<MatchesDto> allMatches = new HashSet<>();
        List<MatchDetailsDto> setOfMatchDetails = new ArrayList<>();

        //call matchhistory api with hero id;
        try {
            allMatches = steamWebApiQueryService.getLast500MatchesForDota2AccountId(dota2AccountId, heroId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while fetching Matches from Steam MatchHistory Api. Error = {}", e.getMessage());
        }

        //Filtering matches based on the gameMode
        //if game mode is 23 that is Turbo mode, removing Ranked Matches to decrease the api calls
        if(gameModeInteger == TURBO_GAME_MODE_ID) {
            allMatches = allMatches.stream().filter(x -> x.getLobby_type() == PUBLIC_MATCHMAKING_LOBBY_TYPE_ID).collect(Collectors.toSet());    //this excludes Ranked matches
        }

        //if matches less than 101 ? proceed ahead : kill the process
        Predicate<Collection<MatchesDto>> p = (x) ->  x.size() < 101;
        if(p.test(allMatches)) {
            //call matchdetails api service for each match
//            allMatches.forEach(match -> setOfMatchDetails.add(steamWebApiQueryService.getMatchDetails(String.valueOf(match.getMatch_id()))));
              allMatches.parallelStream().forEach(match -> setOfMatchDetails.add(steamWebApiQueryService.getMatchDetails(String.valueOf(match.getMatch_id()))));
        } else {
            winRate.setErrorMessage("Too many matches found. Dev pls implement DB logic");
            winRate.setTotalMatchesPlayed(allMatches.size());
            winRate.setHeroName(heroName);
            return winRate;
        }

        //filtering the list based on game_mode received in the inputs
        List<MatchDetailsDto> finalFilteredList = setOfMatchDetails.stream().filter(match -> match.getGame_mode() == gameModeInteger).collect(Collectors.toList());

        //call getWinRateMethod();
        winRateDouble = getWinRate(finalFilteredList, Long.valueOf(dota2AccountId));

        winRate.setGameMode(gameMode);
        winRate.setDota2AccountId(dota2AccountId);
        //TODO work on impl for streamlined logging of steam api calls count
//        winRate.setSteamWebApiCallCount(SteamWebApiQueryService.steamWebApiCallCount);
        winRate.setWinRate(winRateDouble);
        winRate.setTotalMatchesPlayed(setOfMatchDetails.size());
        winRate.setHeroName(heroName);
        return winRate;
    }



    /**
     * This function will provide win rate for today's played matches for given dota2 account ID.
     * This function get Today's played matches and queries steam MatchDetails api for each match played.
     * Note: This function only gives TURBO mode win rate.
     * @return returns win rate percentage in String format
     */
    public ResponseEntity<String> getWinRateOfTodaysPlayedMatches(String dota2AccountId) {
        Set<MatchesDto> allMatches = new HashSet<>();
        long dota2AccountIdLong = Long.parseLong(dota2AccountId);
        int totalNumberOfMatchesPlayed;
        int totalNumberOfMatchesWon;
        Range<Integer> radiantPlayerSlots = Range.of(Range.Bound.inclusive(0), Range.Bound.inclusive(4));

        try {
            allMatches = steamWebApiQueryService.getTodaysPlayedMatches(dota2AccountId);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        Set<Long> matchIds = allMatches.stream().map(MatchesDto::getMatch_id).collect(Collectors.toSet());
        List<MatchDetailsDto> matchDetailsList = steamWebApiQueryService.getMatchDetailsForTodaysMatches(matchIds);

        //Filtering only Turbo games
        List<MatchDetailsDto> finalFilteredList = matchDetailsList.stream().filter(x -> x.getGame_mode() == TURBO_GAME_MODE_ID).collect(Collectors.toList());

        totalNumberOfMatchesPlayed = finalFilteredList.size();
        totalNumberOfMatchesWon = (int) getMatchesWon(finalFilteredList, dota2AccountIdLong);

        return ResponseEntity.ok(String.valueOf(getWinRate(totalNumberOfMatchesPlayed, totalNumberOfMatchesWon)));
    }

    /**
     * This function returns number of matches won by the player which is passed in as dota2AccountId.
     */
    private long getMatchesWon(List<MatchDetailsDto> allMatches, Long dota2AccountId) {
        long matchesWon = 0;

//        allMatches = allMatches.stream().filter(match -> match.getGame_mode() == TURBO_GAME_MODE_ID).collect(Collectors.toList());
        matchesWon = allMatches.stream().filter(match -> hasPlayerWinTheMatch(match, dota2AccountId)).count();

        return matchesWon;
    }

    //made this protected for testing in Junit
    protected boolean hasPlayerWinTheMatch(MatchDetailsDto matchDetails, Long dota2AccountId) {
        boolean winStatus = false;
        boolean radiant_win = matchDetails.isRadiant_win();

        PlayerDto player = matchDetails.getPlayers().stream().filter(x -> x.getAccount_id() == dota2AccountId).findFirst().get();
        if(radiant_win && RADIANT_PLAYER_SLOTS.contains(player.getPlayer_slot())) {
            winStatus = true;
        } else if(!radiant_win && !RADIANT_PLAYER_SLOTS.contains(player.getPlayer_slot())) {
            winStatus = true;
        }
        return winStatus;
    }

    /**
     * @param totalMatches total matches played
     * @param totalMatchesWon total matches won
     * @return This function will return win rate in double data type.
     */
    private double getWinRate(int totalMatches, int totalMatchesWon) {
        return (totalMatchesWon/(double) totalMatches) * 100;
    }

    private double getWinRate(List<MatchDetailsDto> matches, Long dota2AccountId) {
        int totalMatches = 0;
        int totalMatchesWon = 0;

        totalMatches = matches.size();
        totalMatchesWon = (int) getMatchesWon(matches, dota2AccountId);

        return getWinRate(totalMatches, totalMatchesWon);
    }

    /** This function gives top 7 highest win rate heroes for a dota2Account ID for last 30days Turbo matches.
     */
    @LogExecutionTime
    //AOP Logs using custom Annotation
    public Collection<HeroWinRate> getHighestWinRateHeroesForLastMonth(String dota2AccountId) {
        List<HeroWinRate> heroesWinRateList = getAllHeroesWinRates(dota2AccountId, 31);

        return heroesWinRateList.stream()
                .filter(obj -> obj.getTotalMatchesPlayed() > 2)
                .sorted(Comparator.comparing(HeroWinRate::getWinRate).reversed())
                .limit(7)
                .collect(Collectors.toList());
    }


    @LogExecutionTime
    //AOP Logs using custom Annotation
    public Collection<HeroWinRate> getLowestWinRateHeroesForLastWeek(String dota2AccountId) {
        List<HeroWinRate> heroesWinRateList = getAllHeroesWinRates(dota2AccountId, 7);

        return heroesWinRateList.stream()
                .filter(obj -> obj.getTotalMatchesPlayed() > 2)
                .sorted(Comparator.comparing(HeroWinRate::getWinRate))
                .limit(3)
                .collect(Collectors.toList());
    }

    private List<HeroWinRate> getAllHeroesWinRates(String dota2AccountId, int noOfDaysMatches) {
        List<HeroWinRate> heroesWinRateList = new ArrayList<>(7);
        Map<Integer, String> uniqueHeroIdNameMap = new HashMap<>();
        Map<String, Double> heroWinRateMap = new HashMap<>(7);
        Map<Integer, Integer> matchesPlayedOnHeroIdMap = new HashMap<>();
        Map<Integer, Set<Long>> heroIdAndMatchIdsMap = new HashMap<>();
        List<HeroesDto> heroNameList = dota2QueryService.dota2AllHeroesListSupplier.get();
        Collection<MatchesDto> matchesDtoList = new ArrayList<>();

        try {
            //Last 30 days Turbo matches
            matchesDtoList = steamWebApiQueryService.getMatchesForDota2AccountId(dota2AccountId, noOfDaysMatches);
        } catch (Exception e) {
            log.error("WinRateService::getHighestWinRateHeroesForLastMonth() | Exception occurred during fetching all Matches for dota2AccountId -> {}", dota2AccountId);
            e.printStackTrace();
        }

        //Reduce to last 30 days matches
        matchesDtoList = getMatchesPlayedInBetween(LocalDate.now().minusDays(noOfDaysMatches), LocalDate.now(), matchesDtoList);

        //Below snippet finds unique heroes played recently
        matchesDtoList.forEach(match -> {
            Optional<PlayerDto> player = match.getPlayers().stream().filter(playerDto -> playerDto.getAccount_id() == Long.parseLong(dota2AccountId)).findFirst();
            if (player.isPresent()) {
                heroNameList.stream().filter(x -> x.getId() == player.get().getHero_id()).findFirst().ifPresent(x -> uniqueHeroIdNameMap.put(x.getId(), x.getName_loc()));
            }
        });

        //Below code goes through all matches and makes a count of how many times each hero is played from provided matches List.
        //And also makes map of heroId and all matchIds of its respective matches played
        matchesDtoList.forEach(match -> {
            match.getPlayers().stream().filter(playerDto -> playerDto.getAccount_id() == Long.parseLong(dota2AccountId)).findFirst().ifPresent(player -> {
                if (matchesPlayedOnHeroIdMap.containsKey(player.getHero_id())) {
                    Set<Long> tempSet = heroIdAndMatchIdsMap.get(player.getHero_id());
                    tempSet.add(match.getMatch_id());
                    matchesPlayedOnHeroIdMap.put(player.getHero_id(), matchesPlayedOnHeroIdMap.get(player.getHero_id()) + 1);
                    heroIdAndMatchIdsMap.put(player.getHero_id(), tempSet);
                } else {
                    Set<Long> heroIdAndMatchIdsMap2 = new HashSet<>();
                    heroIdAndMatchIdsMap2.add(match.getMatch_id());
                    matchesPlayedOnHeroIdMap.put(player.getHero_id(), 1);
                    heroIdAndMatchIdsMap.put(player.getHero_id(), heroIdAndMatchIdsMap2);
                }
            });
        });

        try {
            uniqueHeroIdNameMap.keySet().forEach(heroId -> {
                int matchesPlayedOnThisHero = matchesPlayedOnHeroIdMap.get(heroId);
                List<MatchDetailsDto> matchDetailsDtoListOnThisHeroId = getMatchDetailsForMatchIds(heroIdAndMatchIdsMap.get(heroId))
                        .stream()
                        .filter(x -> x.getGame_mode() == TURBO_GAME_MODE_ID)
                        .collect(Collectors.toList());
                int matchesWonOnThisHero = (int) getMatchesWon(matchDetailsDtoListOnThisHeroId, Long.parseLong(dota2AccountId));
                double winRateForThisHero = getWinRate(matchesPlayedOnThisHero, matchesWonOnThisHero);

                heroWinRateMap.put(uniqueHeroIdNameMap.get(heroId), winRateForThisHero);

                //Response data to return
                HeroWinRate heroWinRateData = new HeroWinRate();
                heroWinRateData.setHeroName(uniqueHeroIdNameMap.get(heroId));
                heroWinRateData.setWinRate(winRateForThisHero);
                heroWinRateData.setTotalMatchesPlayed(matchesPlayedOnHeroIdMap.get(heroId));
                heroWinRateData.setHeroesDto(heroNameList.stream().filter(heroesDto -> heroesDto.getId() == heroId).findFirst().get());
                heroesWinRateList.add(heroWinRateData);
            });
        } catch (Exception e) {
            log.error("WinRateService::getHighestWinRateHeroesForLastMonth() | Exception during processing Hero WinRate -> {}", dota2AccountId);
            e.printStackTrace();
        }

        log.info("Matches played in last 30 days... Count: {}", matchesDtoList.size());
        log.info("Unique heroes played count={}, in last 30 days ===> {}", uniqueHeroIdNameMap.size(), uniqueHeroIdNameMap);
        log.info("WinRate by Heros Played in last 30 days ===> {}", heroWinRateMap);

        return heroesWinRateList;
    }

    private Collection<MatchesDto> getMatchesPlayedInBetween(LocalDate startDate, LocalDate endDate, Collection<MatchesDto> matchesDtoCollection) {
        return matchesDtoCollection
                .stream()
                .filter(match -> (startDate.isBefore(LocalDateTime.ofEpochSecond(match.getStart_time(), 0, ZoneOffset.ofTotalSeconds(FIVE_AND_HALF_HOURS_TIME_IN_SECONDS)).toLocalDate()) || startDate.isEqual(LocalDateTime.ofEpochSecond(match.getStart_time(), 0, ZoneOffset.ofTotalSeconds(FIVE_AND_HALF_HOURS_TIME_IN_SECONDS)).toLocalDate())
                        && (endDate.isAfter(LocalDateTime.ofEpochSecond(match.getStart_time(), 0, ZoneOffset.ofTotalSeconds(FIVE_AND_HALF_HOURS_TIME_IN_SECONDS)).toLocalDate()) || endDate.isEqual(LocalDateTime.ofEpochSecond(match.getStart_time(), 0, ZoneOffset.ofTotalSeconds(FIVE_AND_HALF_HOURS_TIME_IN_SECONDS)).toLocalDate()))))
                .collect(Collectors.toList());
    }

    private List<MatchDetailsDto> getMatchDetailsForMatchIds(Set<Long> matchIds) {
        return matchIds.parallelStream()
                .map(matchId -> steamWebAPICacheService.getMatchDetails(String.valueOf(matchId)))
                .collect(Collectors.toList());
    }
}
