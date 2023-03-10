package org.energy.abacus.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApplicationScoped
@NamedQueries({
        @NamedQuery(name = "findOutletByIdentifier", query = "SELECT o FROM Outlet o WHERE o.outletIdentifier = :outletIdentifier AND o.userid = :userId"),
        @NamedQuery(name = "findOutletsByUser", query = "SELECT o FROM Outlet o WHERE o.userid = :userId"),
})
public class Outlet {

    //Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private boolean powerOn;

    @Column(unique = true, nullable = false)
    private String userid;


    @Column(unique = true, nullable = false)
    private String outletIdentifier;

    // Relations
    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "outlet",
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private Set<Measurement> measurements;
}
