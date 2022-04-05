//Commented entire class as spring-boot-starter-test dependency is commented in pom.xml

/*
package com.kv.service;

import com.kv.model.dtos.MatchDetailsDto;
import com.kv.model.dtos.PlayerDto;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Range;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class WinRateServiceTest {
    @InjectMocks
    private WinRateService winRateService;

    @Mock
    private SteamWebApiQueryService steamWebApiQueryService;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(winRateService, "RADIANT_PLAYER_SLOTS", Range.of(Range.Bound.inclusive(0), Range.Bound.inclusive(4)));
    }

    @Test
    public void testHasPlayerWinTheMatch() {
        List<Long> playerAccountIds;
        long dota2AccountId = 10000001L;

        PlayerDto player1 = PlayerDto.builder().account_id(dota2AccountId).player_slot(1).build();
        List<PlayerDto> playerList = Lists.newArrayList(player1);
        MatchDetailsDto matchDetailsDto = MatchDetailsDto.builder().radiant_win(true).players(playerList).build();

        // Rad win and Player in rad team
        boolean result = winRateService.hasPlayerWinTheMatch(matchDetailsDto, dota2AccountId);
        assertTrue(result);

        // Rad win and player in dire team
        player1.setPlayer_slot(5);
        boolean result2 = winRateService.hasPlayerWinTheMatch(matchDetailsDto, dota2AccountId);
        assertFalse(result2);

        // Rad lose(Dire win) and player in rad team
        player1.setPlayer_slot(4);
        matchDetailsDto.setRadiant_win(false);
        boolean result3 = winRateService.hasPlayerWinTheMatch(matchDetailsDto, dota2AccountId);
        assertFalse(result3);

        // Rad lose(Dire win) and player in dire team
        player1.setPlayer_slot(9);
        matchDetailsDto.setRadiant_win(false);
        boolean result4 = winRateService.hasPlayerWinTheMatch(matchDetailsDto, dota2AccountId);
        assertTrue(result4);
    }

    private int getRandomAccountId () {
        return (int) (Math.random() * 1_000_000_000);
    }

    private int getRandomPlayerSlot () {
        return (int) (Math.random() * 10);
    }

    private List<PlayerDto> get10Players(List<Long> playerAccountIds) {
        List<PlayerDto> playersList = new ArrayList<>();
        for(int i = 0; i < 10; i++)
            playersList.add(PlayerDto.builder().account_id(playerAccountIds.get(i)).build());
        return playersList;
    }
}*/
