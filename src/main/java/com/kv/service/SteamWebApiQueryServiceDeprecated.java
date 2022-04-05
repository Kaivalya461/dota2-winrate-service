package com.kv.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kv.model.dtos.MatchDetailsDto;
import com.kv.model.dtos.MatchHistoryDto;
import com.kv.model.dtos.MatchesDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Log4j2
//Deprecated(Moved to separate commons project)
public class SteamWebApiQueryServiceDeprecated {

//public class SteamWebApiQueryService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private final static String dota2MatchHistoryUrl = "https://api.steampowered.com/IDOTA2Match_570/GetMatchHistory/v1?key=08737B6505894B0E5A58D8DD2221242E";
    private final static String dota2MatchHistoryBySequenceNumberUrl =
            "https://api.steampowered.com/IDOTA2Match_570/GetMatchHistoryBySequenceNum/v1?key=08737B6505894B0E5A58D8DD2221242E";
    private final static String dota2MatchDetailsUrl = "https://api.steampowered.com/IDOTA2Match_570/GetMatchDetails/v1?key=08737B6505894B0E5A58D8DD2221242E";
    public static long steamWebApiCallCount = 0;

    private static final short ONE_HOUR_TIME_IN_SECONDS = 3600;
    private static final short THREE_HOURS_TIME_IN_SECONDS = ONE_HOUR_TIME_IN_SECONDS * 3;
    private static final short FIVE_AND_HALF_HOURS_TIME_IN_SECONDS = 19800;

    /**
     * This function does a rest call to steam web api and provides MatchHistory details for given Dota2 account ID.
     */
    @Deprecated
    public MatchHistoryDto getMatchHistory(String dota2AccountId, String startAtMatchId, String heroId) {

        MatchHistoryDto matchHistoryDto = new MatchHistoryDto();

        String matchHistoryUrl = buildMatchHistoryUrl(dota2AccountId, startAtMatchId, heroId);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(matchHistoryUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        log.info("Steam Web API called - {}", matchHistoryUrl);
        steamWebApiCallCount += 1;

        try {
            String responseString = null;
            responseString = objectMapper.writeValueAsString(Objects.requireNonNull(response.getBody()).get("result"));
            matchHistoryDto = objectMapper.readValue(responseString, MatchHistoryDto.class);
//            Map<String, Object> map = objectMapper.readValue(responseString, new TypeReference<>() {});
//            matchHistoryDto = objectMapper.convertValue(map, MatchHistoryDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error occurred while converting json response to Java model - {}", e.getMessage());
            e.printStackTrace();
        }

        return matchHistoryDto;
    }

    public MatchDetailsDto getMatchDetails(String matchId) {
        MatchDetailsDto matchDetails = new MatchDetailsDto();
        String url = buildMatchDetailsUrl(matchId);
        ResponseEntity<Map<String, Object>> response = null;

        try {
            response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            log.info("Steam Web API called - {}", url);
            steamWebApiCallCount += 1;
        } catch (Exception e) {
            log.error("Error occurred while fetching data from steam web api");
        }

        try {
            String responseString = null;
            if(response != null && response.hasBody())
                responseString = objectMapper.writeValueAsString(Objects.requireNonNull(response.getBody()).get("result"));
            matchDetails = objectMapper.readValue(responseString, MatchDetailsDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error occurred while converting json response to Java model - {}", e.getMessage());
            e.printStackTrace();
        }
        return matchDetails;
    }

//    /**
//     * This function will return last 500 Matches for given dota2 account Id.
//     */
//    public Set<MatchesDto> getLast500MatchesForDota2AccountId(String dota2AccountId) throws Exception {
//        MatchHistoryDto matchHistoryDto = getMatchHistory(dota2AccountId, null, null);
//        Set<MatchesDto> matchesDtoSet = new HashSet<>();
//        if(containsMatchesPlayed(matchHistoryDto))
//             matchesDtoSet = new HashSet<>(matchHistoryDto.getMatches());
//        else if(matchHistoryDto.getStatusDetail() != null) {
//            throw new Exception(matchHistoryDto.getStatusDetail());
//        }
//        //Setting 0 at the start of the method to get total rest calls made for single accountId
//        steamWebApiCallCount = 0;
//
//        while(matchHistoryDto.getStatus() == 1 && matchHistoryDto.getResults_remaining() != 0) {
//            long startAtMatchId = Objects.requireNonNull(matchHistoryDto.getMatches().stream().skip(matchHistoryDto.getMatches().size()-1).findFirst().orElse(null)).getMatch_id();
//            matchHistoryDto = getMatchHistory(dota2AccountId, startAtMatchId + "", null);
//            matchesDtoSet.addAll(matchHistoryDto.getMatches());
//        }
//
//        return matchesDtoSet;
//    }

    /**
     * This function will return last 500 Matches for given dota2 account Id and for a specific Dota2 hero id only.
     */
    //In-progress
    public Set<MatchesDto> getLast500MatchesForDota2AccountId(String dota2AccountId, String heroId) throws Exception {
        MatchHistoryDto matchHistoryDto = getMatchHistory(dota2AccountId, null, heroId);
        Set<MatchesDto> matchesDtoSet = new HashSet<>();
        if(containsMatchesPlayed(matchHistoryDto))
            matchesDtoSet = new HashSet<>(matchHistoryDto.getMatches());
        else if(matchHistoryDto.getStatusDetail() != null) {
            throw new Exception(matchHistoryDto.getStatusDetail());
        }
        //Setting 0 at the start of the method to get total rest calls made for single accountId
        steamWebApiCallCount = 0;

        while(matchHistoryDto.getStatus() == 1 && matchHistoryDto.getResults_remaining() != 0) {
            long startAtMatchId = Objects.requireNonNull(matchHistoryDto.getMatches().stream().skip(matchHistoryDto.getMatches().size()-1).findFirst().orElse(null)).getMatch_id();
            matchHistoryDto = getMatchHistory(dota2AccountId, startAtMatchId + "", null);
            matchesDtoSet.addAll(matchHistoryDto.getMatches());
        }

        return matchesDtoSet;
    }

    /**
     * This function will return all today's played Matches for given dota2 account Id. (Time Period is 0300 to 2400 plus next day's +3 hours)
     * Note: If player not played any matches today then it will return matches for the day which player last played.
     */
    public Set<MatchesDto> getTodaysPlayedMatches(String dota2AccountId) throws Exception {
        Set<MatchesDto> matchesDtoSet = new HashSet<>();
        List<MatchesDto> filteredMatches = new ArrayList<>();
        LocalDateTime latestMatchPlayedDate = null;

        MatchHistoryDto matchHistory = getMatchHistory(dota2AccountId, null, null);
        MatchesDto latestMatchPlayed = new MatchesDto();

        if(containsMatchesPlayed(matchHistory))
            latestMatchPlayed = matchHistory.getMatches().get(0);
        else if(matchHistory.getStatusDetail() != null)
            throw new Exception(matchHistory.getStatusDetail());


        //Finding the recent date for which the player has played some matches.
        //All Matches played after 3AM of this date are taken into processing.
        if(latestMatchPlayed != null) {
            latestMatchPlayedDate = LocalDateTime.ofEpochSecond(
                    latestMatchPlayed.getStart_time(), 0, ZoneOffset.ofTotalSeconds(FIVE_AND_HALF_HOURS_TIME_IN_SECONDS));  //Converting epoch time to IST LocalDateTime

            if(latestMatchPlayedDate.toLocalTime().isBefore(LocalTime.ofSecondOfDay(THREE_HOURS_TIME_IN_SECONDS)))
                latestMatchPlayedDate = latestMatchPlayedDate.minusDays(1); //Moving date 1 day in past as we mostly play some matches after 12am for that day...only if last match is played between 12am to 3am

            latestMatchPlayedDate = LocalDateTime.of(latestMatchPlayedDate.toLocalDate(), LocalTime.ofSecondOfDay(THREE_HOURS_TIME_IN_SECONDS));
        }

        //Passing above calculated DateTime, and filtering all the matches which are played after this time.
        if(latestMatchPlayedDate != null) {
            filteredMatches = getMatchesPlayedOfTheDay(latestMatchPlayedDate, matchHistory.getMatches());
            log.info("Total Matches played for that day({}): {}", latestMatchPlayedDate.toLocalDate(), filteredMatches.size());
        }

        //Logging date/time details for first and last matches of the day
        if(!filteredMatches.isEmpty())
            logDateTimeDetailsForFirstAndLastMatches(filteredMatches);

        matchesDtoSet = new HashSet<>(filteredMatches);

        return matchesDtoSet;
    }

    //In-progress
    public List<MatchDetailsDto> getMatchDetailsForTodaysMatches(Set<Long> matchIds) {
        return matchIds.stream()
                .map(matchId -> getMatchDetails(String.valueOf(matchId)))
                .collect(Collectors.toList());
    }

    private void logDateTimeDetailsForFirstAndLastMatches(List<MatchesDto> matchesDtoList) {
        Consumer<List<MatchesDto>> logFirstAndLastMatchPlayedDateDetails = matches -> {
            //converting epoch time to IST LocalDateTime
            LocalDateTime firstMatchOfTheDayTime = LocalDateTime.ofEpochSecond(matches.stream().map(MatchesDto::getStart_time).min(Long::compare).get(), 0, ZoneOffset.ofTotalSeconds(FIVE_AND_HALF_HOURS_TIME_IN_SECONDS));
            LocalDateTime lastMatchOfTheDayTime = LocalDateTime.ofEpochSecond(matches.stream().map(MatchesDto::getStart_time).max(Long::compare).get(), 0, ZoneOffset.ofTotalSeconds(FIVE_AND_HALF_HOURS_TIME_IN_SECONDS));

            if(firstMatchOfTheDayTime.equals(lastMatchOfTheDayTime)) {
                log.info("Only 1 match played bruh.. {}", firstMatchOfTheDayTime);
            }
            log.info("First Match of the day was played at {}", firstMatchOfTheDayTime);
            log.info("Last Match of the day was played at {}", lastMatchOfTheDayTime);
        };

        logFirstAndLastMatchPlayedDateDetails.accept(matchesDtoList);
    }

    /**
     * This function will return all the matches who start time is greater than requestedDate.
     */
    private List<MatchesDto> getMatchesPlayedOfTheDay(LocalDateTime requestedDate, List<MatchesDto> allMatches) {
        return allMatches.stream().filter(x -> x.getStart_time() >= requestedDate.toEpochSecond(ZoneOffset.ofTotalSeconds(FIVE_AND_HALF_HOURS_TIME_IN_SECONDS))).collect(Collectors.toList());
    }

    private boolean containsMatchesPlayed(MatchHistoryDto matchHistoryDto) {
        Predicate<MatchHistoryDto> predicate = x -> x.getStatus() == 1 && x.getMatches() != null;
        return predicate.test(matchHistoryDto);
    }


    /**
     * This function builds url for MatchHistory steam web api
     */
    private String buildMatchHistoryUrl(String dota2AccountId, String startAtMatchId, String heroId) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(SteamWebApiQueryServiceDeprecated.dota2MatchHistoryUrl).queryParam("account_id", dota2AccountId);

        if(startAtMatchId != null && !startAtMatchId.isEmpty())
            uriComponentsBuilder.queryParam("start_at_match_id", startAtMatchId);
        if(heroId != null && !heroId.isEmpty())
            uriComponentsBuilder.queryParam("hero_id", heroId);

        return uriComponentsBuilder.toUriString();
    }

    /**
     * This function builds url for MatchDetails steam web api
     */
    private String buildMatchDetailsUrl(String matchId) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(SteamWebApiQueryServiceDeprecated.dota2MatchDetailsUrl)
                .queryParam("match_id", matchId);
        return uriComponentsBuilder.toUriString();
    }

    /**
     * This function builds url for MatchHistoryBySequenceNumber steam web api
     */
    private String buildMatchHistoryBySequenceNumberUrl(long matchSeqNum) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(SteamWebApiQueryServiceDeprecated.dota2MatchHistoryBySequenceNumberUrl).queryParam("start_at_match_seq_num", matchSeqNum);
        return uriComponentsBuilder.toUriString();
    }

    /**
     * This function builds url for MatchHistoryBySequenceNumber steam web api
     */
    private String buildMatchHistoryBySequenceNumberUrl(long startAtMatchSeqNum, short numberOfMatchesRequested) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(SteamWebApiQueryServiceDeprecated.dota2MatchHistoryBySequenceNumberUrl)
                .queryParam("start_at_match_seq_num", startAtMatchSeqNum)
                .queryParam("matches_requested", numberOfMatchesRequested);
        return uriComponentsBuilder.toUriString();
    }

    /**
     * This function will return all the Matches from start to end MatchSequenceNumber.
     */
    //In progress
    private List<MatchDetailsDto> getMatchDetailsList(long startMatchSequenceNumber, long endMatchSequenceNumber, String dota2AccountId) {
        List<MatchDetailsDto> matchDetailsDtoList = null;
        String url = buildMatchHistoryBySequenceNumberUrl(startMatchSequenceNumber);
        return matchDetailsDtoList;
    }

    /**
     * This function will return last 500 Matches for given dota2 account Id.
     */
    //In-progress?????????????
    public Set<MatchesDto> getLastPlayedDaysMatchesForDota2AccountId(String dota2AccountId) {
        return null;
    }

}
