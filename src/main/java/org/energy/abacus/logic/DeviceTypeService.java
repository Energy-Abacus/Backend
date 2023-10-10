package org.energy.abacus.logic;

import org.energy.abacus.entities.DeviceType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Collection;

@ApplicationScoped
public class DeviceTypeService {

    @Inject
    EntityManager entityManager;

    public Collection<DeviceType> getDeviceTypeList(){
        return entityManager.createNamedQuery("findAllDeviceTypes")
                .getResultList();
    }
}
