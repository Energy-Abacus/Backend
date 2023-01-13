package org.energy.abacus.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.*;
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
