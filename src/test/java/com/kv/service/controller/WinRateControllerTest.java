package com.kv.service.controller;

import com.kv.controller.WinRateController;
import com.kv.winrate.dto.HeroWinRate;
import com.kv.service.WinRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WinRateController.class)
public class WinRateControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockBean
    private WinRateService winRateService;

    @Test
    void getLowestWinRateHeroesForLastWeekTest() throws Exception {
        // data
        String dota2AccountId = "12312312";
        HeroWinRate heroWinRate = new HeroWinRate();
        heroWinRate.setHeroName("Axe");
        heroWinRate.setDota2AccountId(dota2AccountId);
        heroWinRate.setWinRate(55.55);
        Collection<HeroWinRate> collection = List.of(heroWinRate);

        // when/then
        when(winRateService.getLowestWinRateHeroesForLastWeek(anyString()))
                .thenReturn(collection);

        mockMvc.perform(get("/win-rate/lowestWinRateHeroes/12312312"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dota2AccountId").value(dota2AccountId))
                .andExpect(jsonPath("$[0].winRate").value(55.55));
    }
}
