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
public class MatchesDto {
    private long match_id;
    private long match_seq_num;
    private long start_time;
    private int lobby_type;
    List<PlayerDto> players;
}
