package com.kv.service;

import com.kv.matchdetails.dto.MatchDetailsDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j2
public class SteamWebAPICacheService {

    @Autowired
    private SteamWebApiQueryService steamWebApiQueryService;

    /**
     * This method is created for caching the MatchDetails data
     * */
    @Cacheable(value = "steam.web.api.match-details")
    public MatchDetailsDto getMatchDetails(String matchId, Optional<Long> matchSeqNumOpt) {
        log.warn("Cache failed, calling SteamWebApiQueryService for fetching Match Details for matchId: {}", matchId);
        return steamWebApiQueryService.getMatchDetails(matchId, matchSeqNumOpt);
    }
}
