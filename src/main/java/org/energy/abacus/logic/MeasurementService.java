package org.energy.abacus.logic;

import lombok.extern.java.Log;
import org.energy.abacus.dtos.MeasurementDto;
import org.energy.abacus.entities.Hub;
import org.energy.abacus.entities.Measurement;
import org.energy.abacus.entities.Outlet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.NotAllowedException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
@Log
public class MeasurementService {

    @Inject
    EntityManager entityManager;

    @Inject
    HubService hubService;

    @Inject
    OutletService outletService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");


    public void addNewMeasurement(final MeasurementDto measurementDto) {
        Hub hub = hubService.getHubByToken(measurementDto.getPostToken());

        if (hub == null) {
            throw new NotAllowedException("Wrong token!");
        }

        Outlet outlet = outletService.getOutlet(measurementDto.getOutletIdentifier(), hub.getId());
        double wattPower = Double.parseDouble(measurementDto.getWattPower());
        List<Double> results = entityManager.createNamedQuery("findTotalPowerUsedUntilNow", Double.class)
                .setParameter("outletId", outlet.getId())
                .setMaxResults(1)
                .getResultList();
        double powerUsedUntilNow = results.isEmpty() ? 0 : results.get(0);

        Measurement measurementEntity = Measurement.builder()
                .timeStamp(LocalDateTime.parse(measurementDto.getTimeStamp(), DATE_FORMAT))
                .powerOn(measurementDto.getPowerOn().equals("on"))
                .wattPower(wattPower)
                .totalPowerUsed(powerUsedUntilNow + wattPower)
                .temperature(Double.parseDouble(measurementDto.getTemperature()))
                .outlet(outlet)
                .build();
        entityManager.persist(measurementEntity);
    }

    public List<Measurement> getMeasurementsByOutletInTimeFrame(int outletId, int from, int to, String userId) {
        return entityManager.createNamedQuery("findMeasurementsByOutletInTimeFrame", Measurement.class)
                .setParameter("outletId", outletId)
                .setParameter("userId", userId)
                .setParameter("from", LocalDateTime.ofInstant(Instant.ofEpochSecond(from), ZoneId.of("UTC")))
                .setParameter("to", LocalDateTime.ofInstant(Instant.ofEpochSecond(to), ZoneId.of("UTC")))
                .getResultList();
    }
}
