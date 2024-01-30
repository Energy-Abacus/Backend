package org.energy.abacus.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.energy.abacus.entities.DeviceType;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetOutletDto {
    private int id;
    private String name;
    private String outletIdentifier;
    private int hubId;
    private boolean powerOn;
    private double totalPowerUsed;
    private double avgPowerUsed;
    private List<DeviceType> deviceTypes;
}
