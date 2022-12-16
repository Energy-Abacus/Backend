package org.energy.abacus.User;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.energy.abacus.Outlets.Outlet;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@ApplicationScoped
public class User extends PanacheEntityBase {

    //Fields
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String username;

    //Relations
    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "user",
            fetch = FetchType.LAZY
    )
    private Set<Outlet> outlets;
}
