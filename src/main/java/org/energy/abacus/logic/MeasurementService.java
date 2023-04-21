package org.energy.abacus.logic;

import lombok.extern.java.Log;
import org.energy.abacus.dtos.HubDto;
import org.energy.abacus.dtos.MeasurementDto;
import org.energy.abacus.dtos.OutletDto;
import org.energy.abacus.entities.Hub;
import org.energy.abacus.entities.Measurement;
import org.energy.abacus.entities.Outlet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.NotAllowedException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

@ApplicationScoped
@Log
public class MeasurementService {

    @Inject
    EntityManager entityManager;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");


    public void addNewMeasurement(final MeasurementDto measurementDto) {
        Hub hub = getHubByToken(measurementDto.getPostToken());

        if (hub == null) {
            throw new NotAllowedException("Wrong token!");
        }

        Outlet outlet = getOutlet(measurementDto.getOutletIdentifier(), hub.getId());
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

    public int addNewOutlet(final OutletDto outletDTO, String userId) {
        Hub hub = getHubById(outletDTO.getHubId(), userId);
        Outlet outletEntity = Outlet.builder()
                .name(outletDTO.getName())
                .outletIdentifier(outletDTO.getOutletIdentifier())
                .hub(hub)
                .build();
        entityManager.persist(outletEntity);
        return outletEntity.getId();
    }

    public Hub addNewHub(final HubDto hubDto, String userId) {
        Hub hubEntity = Hub.builder()
                .name(hubDto.getName())
                .postToken(generateToken())
                .userid(userId)
                .build();
        entityManager.persist(hubEntity);
        return hubEntity;
    }

    public Hub getHubByToken(String postToken) {
        List<Hub> hubs = entityManager.createNamedQuery("findHubByToken", Hub.class)
                .setParameter("token", postToken)
                .getResultList();

        return hubs.isEmpty() ? null : hubs.get(0);
    }

    public Hub getHubById(int id, String userId) {
        Hub hub = entityManager.createNamedQuery("findHubById", Hub.class)
                .setParameter("id", id)
                .getSingleResult();

        if (!hub.getUserid().equals(userId)) {
            throw new NotAllowedException("Hub does not belong to user");
        }
        return hub;
    }

    public List<Hub> getAllHubsForUser(String userId) {
        return entityManager.createNamedQuery("findHubsByUserId", Hub.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public Outlet getOutlet(String outletIdentifier, int hubId) {
        return entityManager.createNamedQuery("findOutletByIdentifier", Outlet.class)
                .setParameter("outletIdentifier", outletIdentifier)
                .setParameter("hubId", hubId)
                .getSingleResult();
    }

    public Collection<Outlet> getAllOutletsForHub(int hubId, String userId) {
        Hub hub = getHubById(hubId, userId);
        return hub.getOutlets();
        /*return entityManager.createNamedQuery("findOutletsByHubId", Outlet.class)
                .setParameter("hubId", hubId)
                .getResultList();*/
    }

    public List<Measurement> getMeasurementsByOutletInTimeFrame(int outletId, int from, int to, String userId) {
        return entityManager.createNamedQuery("findMeasurementsByOutletInTimeFrame", Measurement.class)
                .setParameter("outletId", outletId)
                .setParameter("userId", userId)
                .setParameter("from", LocalDateTime.ofInstant(Instant.ofEpochSecond(from), ZoneId.of("UTC")))
                .setParameter("to", LocalDateTime.ofInstant(Instant.ofEpochSecond(to), ZoneId.of("UTC")))
                .getResultList();
    }

    public String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return Base64.getEncoder().encodeToString(token);
    }
}
