package com.kv.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDetailsDto {
    private long match_id;
    private long match_sequence_num;
    private boolean radiant_win;
    private int lobby_type;
    private int game_mode;
    private List<PlayerDto> players;
}
