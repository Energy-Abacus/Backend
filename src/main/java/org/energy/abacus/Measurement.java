package org.energy.abacus;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
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
public class Measurement extends PanacheEntity {

    private double power;
}
