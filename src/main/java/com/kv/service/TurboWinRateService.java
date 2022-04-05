package com.kv.service;

import com.kv.model.HeroWinRate;
import com.kv.matchdetails.dto.MatchesDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Log4j2
public class TurboWinRateService {

    @Autowired
    private SteamWebApiQueryService steamWebApiQueryService;

    private static final int TURBO_GAME_MODE_ID = 23;
    private final Range<Integer> RADIANT_PLAYER_SLOTS = Range.of(Range.Bound.inclusive(0), Range.Bound.inclusive(4));

    /**
     * This function will return WinRate for given Dota2 account ID.
     */
    //In Progress
    public HeroWinRate getTurboModeWinRateForLast500Matches(String dota2AccountId) {
        log.info("Entering TurboModeWinRateForLast500Matches service method");
        HeroWinRate turboWinRate = HeroWinRate.builder().dota2AccountId(dota2AccountId).build();
        double winRate = 0;
        int totalMatches = 0;
        int totalMatchesWon = 0;


        long latestMatchSequenceNumber;
        long oldestMatchSequenceNumber;

        List<MatchesDto> last500MatchesListPlayed = null;
        try {
            last500MatchesListPlayed = new ArrayList<>(steamWebApiQueryService.getLast500MatchesForDota2AccountId(dota2AccountId));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Get oldest and latest matchSequenceNumber required for fetching MatchDetails(this contains which team won)
        //and MatchDetails will be found using Steam getMatchHistoryBySequenceNumber web api
        List<Long> matchIdList = last500MatchesListPlayed.stream().map(MatchesDto::getMatch_id).collect(Collectors.toList());
        latestMatchSequenceNumber = Objects.requireNonNull(matchIdList.stream().max(Long::compare).orElse(null));
        oldestMatchSequenceNumber = Objects.requireNonNull(matchIdList.stream().min(Long::compare).orElse(null));

        //call getMatchHistoryBySequenceNumber steam web api for all the last 500 matches as steam only allows 100 matches per request,
        totalMatches = last500MatchesListPlayed.size();

        winRate = getWinRate(totalMatches, totalMatchesWon);
        turboWinRate.setWinRate(winRate);
        //TODO work on impl for streamlined logging of steam api calls count
//        turboWinRate.setSteamWebApiCallCount(SteamWebApiQueryService.steamWebApiCallCount);
        return turboWinRate;
    }

    //Function dev in progress
    private int getTotalMatchesPlayed() {
        int totalMatchesPlayed = 0;

        return totalMatchesPlayed;
    }

    /**
     * @param totalMatches total matches played
     * @param totalMatchesWon total matches won
     * @return This function will return win rate in double data type.
     */
    private double getWinRate(int totalMatches, int totalMatchesWon) {
        return (totalMatchesWon/(double) totalMatches) * 100;
    }
}
