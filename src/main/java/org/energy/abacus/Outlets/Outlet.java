package org.energy.abacus.Outlets;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.smallrye.common.constraint.NotNull;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.energy.abacus.Measurements.Measurement;
import org.energy.abacus.User.User;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@ApplicationScoped
public class Outlet extends PanacheEntityBase {

    //Fields
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean powerOn;

    //Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id")
    private User user;

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "outlet",
            fetch = FetchType.LAZY
    )
    private Set<Measurement> measurements;
}
