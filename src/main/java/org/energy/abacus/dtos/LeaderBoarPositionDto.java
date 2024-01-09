package org.energy.abacus.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaderBoarPositionDto {
    private UserDto user;
    private int position;
    private double powerUsed;
}
