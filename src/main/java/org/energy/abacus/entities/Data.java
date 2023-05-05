package org.energy.abacus.entities;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Measurement(name = "data")
public class Data {

    @Column(name = "timeStamp", timestamp = true)
    private Instant timeStamp;

    @Column(name = "wattPower")
    private double wattPower;

    @Column(name = "totalPowerUsed")
    private double totalPowerUsed;

    @Column(name = "temperature")
    private double temperature;

    @Column(name = "outletId", tag = true)
    private String outletId;

}
