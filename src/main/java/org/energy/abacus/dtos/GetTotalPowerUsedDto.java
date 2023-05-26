package org.energy.abacus.dtos;

import lombok.Getter;

@Getter
public class GetTotalPowerUsedDto {
    private String postToken;
    private String outletIdentifier;
    private long from;
    private long to;
}
