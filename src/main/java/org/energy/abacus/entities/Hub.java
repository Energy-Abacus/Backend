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
        @NamedQuery(name = "findHubByToken", query = "SELECT h FROM Hub h WHERE h.postToken = :token"),
        @NamedQuery(name = "findHubById", query = "SELECT h FROM Hub h WHERE h.id = :id"),
        @NamedQuery(name = "findHubsByUserId", query = "SELECT h FROM Hub h WHERE h.userid = :userId"),
})
public class Hub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String postToken;

    @Column(unique = true, nullable = false)
    private String userid;

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "hub",
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private Set<Outlet> outlets;

}
