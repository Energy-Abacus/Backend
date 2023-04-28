package org.energy.abacus.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.*;


import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Measurement(name = "data")
public class Data {

    @Column(name = "timeStamp")
    private LocalDateTime timeStamp;

    @Column(name = "powerOn")
    private boolean powerOn;

    @Column(name = "wattPower")
    private double wattPower;

    @Column(name = "totalPowerUsed")
    private double totalPowerUsed;

    @Column(name = "temperature")
    private double temperature;

    @Column(name = "outletId",tag = true)
    private int outletId;

}
