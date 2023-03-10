package org.energy.abacus.dtos;

import lombok.Getter;

@Getter
public class MeasurementDto {

    private String timeStamp;

    private boolean powerOn;

    private double wattPower;

    private double wattMinutePower;

    private double temperature;

    private String outletIdentifier;

    private String postToken;
}
