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
    private double power;

    //Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_outlet_id")
    private Outlet outlet;
}
