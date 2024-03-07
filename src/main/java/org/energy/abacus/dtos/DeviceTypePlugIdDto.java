package org.energy.abacus.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.energy.abacus.entities.DeviceType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTypePlugIdDto {
    private String deviceType;
    private int plugId;
}
