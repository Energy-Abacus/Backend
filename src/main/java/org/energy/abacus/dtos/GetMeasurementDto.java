package org.energy.abacus.dtos;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GetMeasurementDto {
    private int outletId;
    private String from;
    private String to;
}
