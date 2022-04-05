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
public class MatchHistoryDto {
    private int status;//1 - success status
    private String statusDetail;
    private int num_results;
    private int total_results;
    private int results_remaining;
    List<MatchesDto> matches;
}
