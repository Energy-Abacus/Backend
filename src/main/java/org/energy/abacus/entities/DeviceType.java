package org.energy.abacus.entities;


import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@NamedQuery(name = "findAllDeviceTypes", query = "SELECT d FROM DeviceType d")
public class DeviceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    private double expectedConsumption;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] icon;

    @ManyToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<Outlet> outlets;
}
