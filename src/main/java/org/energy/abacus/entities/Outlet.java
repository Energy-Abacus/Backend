package org.energy.abacus.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name = "findOutletByIdentifier", query = "SELECT o FROM Outlet o WHERE o.outletIdentifier = :outletIdentifier AND o.hubId = :hubId"),
        @NamedQuery(name = "findOutletsByHubId", query = "SELECT o FROM Outlet o WHERE o.hubId = :hubId")
})
public class Outlet {

    //Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private boolean powerOn;

    @Column(unique = true, nullable = false)
    private String outletIdentifier;

    @Column(name = "hubId", insertable = false, updatable = false)
    private int hubId;

    @ManyToOne
    @JoinColumn(name = "hubId")
    @JsonIgnore
    private Hub hub;
}
