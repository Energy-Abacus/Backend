package org.energy.abacus.logic;

import org.energy.abacus.entities.DeviceType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class DeviceTypeService {

    @Inject
    EntityManager entityManager;

    public Collection<DeviceType> getDeviceTypeList(){
        return entityManager.createNamedQuery("findAllDeviceTypes", DeviceType.class)
                .getResultList();
    }

    public List<DeviceType> getDeviceTypesById(List<Long> deviceTypeIds) {
        return entityManager.createNamedQuery("findAllDeviceTypesById", DeviceType.class)
                .setParameter("ids", deviceTypeIds)
                .getResultList();
    }
}
