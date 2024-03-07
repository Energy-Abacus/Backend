package org.energy.abacus.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.energy.abacus.entities.DeviceType;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendInfoDto {
    private int rank;
    private double lastDayConsumption;
    private double totalConsumption;
    private List<DeviceTypePlugIdDto> deviceTypes;
}
