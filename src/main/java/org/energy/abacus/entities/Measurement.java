package org.energy.abacus.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedQueries({
        @NamedQuery(name = "findMeasurementsByOutlet", query = "SELECT m FROM Measurement m " +
                "WHERE m.outletId = :outletId " +
                "AND m.outlet.hub.userid = :userId"),
        @NamedQuery(name = "findMeasurementsByOutletInTimeFrame", query = "SELECT m FROM Measurement m " +
                "WHERE m.outletId = :outletId " +
                "AND m.outlet.hub.userid = :userId " +
                "AND m.timeStamp BETWEEN :from AND :to"
        ),
})
public class Measurement {

    //Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private LocalDateTime timeStamp;

    @Column(nullable = false)
    private boolean powerOn;

    @Column(nullable = false)
    private double wattPower;

    @Column(nullable = false)
    private double wattMinutePower;

    @Column(nullable = false)
    private double temperature;

    @Column(name = "outletId", insertable = false, updatable = false)
    private int outletId;

    //Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outletId")
    @JsonIgnore
    private Outlet outlet;
}
