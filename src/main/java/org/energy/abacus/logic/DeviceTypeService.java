package org.energy.abacus.logic;

import org.energy.abacus.entities.DeviceType;
import org.energy.abacus.entities.Outlet;

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

    public int connectDeviceTypeToOutlet(int deviceTypeId, int outletId) {
        DeviceType deviceType = entityManager.find(DeviceType.class, deviceTypeId);
        if(deviceType == null){
            throw new IllegalArgumentException("Device type with id " + deviceTypeId + " does not exist");
        }
        var outlet = entityManager.find(Outlet.class, outletId);
        outlet.getDeviceTypes().add(deviceType);
        deviceType.getOutlets().add(outlet);
        return deviceType.getId();
    }
}
