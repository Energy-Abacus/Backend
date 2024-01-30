package org.energy.abacus.dtos;

import lombok.Getter;

import java.util.List;

@Getter
public class OutletDto {
    private String name;
    private String outletIdentifier;
    private int hubId;
    private List<Long> deviceTypeIds;
}
