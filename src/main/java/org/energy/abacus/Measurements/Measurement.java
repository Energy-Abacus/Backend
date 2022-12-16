package org.energy.abacus.Measurements;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.energy.abacus.Outlets.Outlet;

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
