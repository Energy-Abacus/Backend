package org.energy.abacus.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.energy.abacus.entities.DeviceType;
import org.energy.abacus.entities.Friendship;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Collection;

@ApplicationScoped
public class DeviceTypeService {

    @Inject
    EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Collection<DeviceType> getDeviceTypeList(){
        return entityManager.createQuery("SELECT d FROM DeviceType d", DeviceType.class).getResultList();
    }
}
