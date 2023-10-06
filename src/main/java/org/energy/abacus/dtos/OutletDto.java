package org.energy.abacus.dtos;

import lombok.Getter;
import org.energy.abacus.entities.DeviceType;

import java.util.List;

@Getter
public class OutletDto {
    private String name;
    private String outletIdentifier;
    private int hubId;
    private List<DeviceType> deviceTypes;
}
