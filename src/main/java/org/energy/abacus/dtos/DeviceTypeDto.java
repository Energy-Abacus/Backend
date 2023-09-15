package org.energy.abacus.dtos;

import lombok.Getter;

@Getter
public class DeviceTypeDto {
    private String name;
    private double expectedConsumption;
    private byte[] icon;
}
