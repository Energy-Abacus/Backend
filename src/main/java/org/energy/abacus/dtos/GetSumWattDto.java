package org.energy.abacus.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetSumWattDto {
    private Instant timeStamp;
    private double wattPowerSum;
}
