package org.energy.abacus.dtos;

import lombok.Getter;

@Getter
public class MeasurementDto {

    private String timeStamp;

    private String powerOn;

    private String wattPower;

    private String temperature;

    private String outletIdentifier;

    private String postToken;
}
