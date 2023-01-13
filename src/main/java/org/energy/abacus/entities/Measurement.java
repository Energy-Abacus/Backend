package org.energy.abacus.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@ApplicationScoped
public class Measurement extends PanacheEntityBase {

    //Fields
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private boolean status;

    @Column(nullable = false)
    private double wattPower;

    @Column(nullable = false)
    private double wattMinutePower;

    @Column(nullable = false)
    private double temperature;

    //Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_outlet_id")
    private Outlet outlet;
}
