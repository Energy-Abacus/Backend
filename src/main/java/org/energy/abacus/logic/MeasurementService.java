package org.energy.abacus.logic;

import lombok.extern.java.Log;
import org.energy.abacus.dtos.MeasurementDto;
import org.energy.abacus.dtos.OutletDTO;
import org.energy.abacus.entities.Measurement;
import org.energy.abacus.entities.Outlet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
@Log
public class MeasurementService {

    @Inject
    EntityManager entityManager;

    public void addNewMeasurement(final MeasurementDto measurementDto, String userId) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        Measurement measurementEntity = Measurement.builder()
                .timeStamp(LocalDateTime.parse(measurementDto.getTimeStamp(), dateFormatter))
                .powerOn(measurementDto.isPowerOn())
                .wattPower(measurementDto.getWattPower())
                .wattMinutePower(measurementDto.getWattMinutePower())
                .temperature(measurementDto.getTemperature())
                .outlet(getOutlet(measurementDto.getOutletIdentifier(), userId))
                .build();
        entityManager.persist(measurementEntity);
    }

    public int addNewOutlet(final OutletDTO outletDTO, String userId) {
        Outlet outletEntity = Outlet.builder()
                .name(outletDTO.getName())
                .userid(userId)
                .outletIdentifier(outletDTO.getOutletIdentifier())
                .build();
        entityManager.persist(outletEntity);
        return outletEntity.getId();
    }

    public Outlet getOutlet(String outletIdentifier, String userId) {
        return entityManager.createNamedQuery("findOutletByIdentifier", Outlet.class)
                .setParameter("outletIdentifier", outletIdentifier)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public List<Outlet> getAllOutletsForUser(String userId) {
        return entityManager.createNamedQuery("findOutletsByUser", Outlet.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Measurement> getMeasurementsByOutlet(int outletId) {
        return entityManager.createNamedQuery("findMeasurementsByOutlet", Measurement.class)
                .setParameter("outletId", outletId)
                .getResultList();
    }
}
