package org.energy.abacus.logic;

import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.energy.abacus.dtos.GetTotalPowerUsedDto;
import org.energy.abacus.dtos.MeasurementDto;
import org.energy.abacus.entities.Hub;
import org.energy.abacus.entities.Data;
import org.energy.abacus.entities.Outlet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.NotAllowedException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Log
public class MeasurementService {

    @Inject
    EntityManager entityManager;

    @Inject
    HubService hubService;

    @Inject
    OutletService outletService;

    private InfluxDBClient influxDBClient;
    @ConfigProperty(name = "influxdb.url")
    String connectionUrl;
    @ConfigProperty(name = "influxdb.token")
    String token;
    @ConfigProperty(name = "influxdb.org-id")
    String orgId;
    @ConfigProperty(name = "influxdb.bucketId")
    String bucketId;
    @ConfigProperty(name = "influxdb.bucket")
    String bucketName;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    Logger logger = LoggerFactory.getLogger(MeasurementService.class);
    public static final Long DATA_RETENTION_DAYS = -365L;

    @PostConstruct
    private void initializeInfluxDBClient() {
        logger.info("Connecting to: {}, token: {}, org: {}, bucketId: {}",
                connectionUrl, token, orgId, bucketId);
        this.influxDBClient = InfluxDBClientFactory.create(connectionUrl, token.toCharArray(), orgId, bucketId);
    }

    public void addNewMeasurement(final MeasurementDto measurementDto) {
        Hub hub = hubService.getHubByToken(measurementDto.getPostToken());

        if (hub == null) {
            throw new NotAllowedException("Wrong token!");
        }

        Outlet outlet = outletService.getOutlet(measurementDto.getOutletIdentifier(), hub.getId());
        double wattPower = Double.parseDouble(measurementDto.getWattPower());


        WriteApi writeApi = influxDBClient.getWriteApi();

        Data measurement = new Data();
        measurement.setTimeStamp(LocalDateTime.parse(measurementDto.getTimeStamp(), DATE_FORMAT));
        measurement.setPowerOn(measurementDto.getPowerOn().equals("on"));
        measurement.setWattPower(wattPower);
        measurement.setTotalPowerUsed(wattPower);
        measurement.setTemperature(Double.parseDouble(measurementDto.getTemperature()));
        measurement.setOutletId(outlet.getId());

        writeApi.writeMeasurement(WritePrecision.NS, measurement);
        writeApi.close();
    }

    public List<Data> getMeasurementsByOutletInTimeFrame(int outletId, int from, int to, String userId) {

        String temperatureByLocationQuery = Flux.from(bucketName)
                .range(DATA_RETENTION_DAYS, ChronoUnit.DAYS)
                .filter(Restrictions.and(Restrictions.tag("outletId").equal(outletId)))
                .filter(Restrictions.and(Restrictions.tag("from").equal(LocalDateTime.ofInstant(Instant.ofEpochSecond(from), ZoneId.of("UTC")))))
                .filter(Restrictions.and(Restrictions.tag("to").equal(LocalDateTime.ofInstant(Instant.ofEpochSecond(to), ZoneId.of("UTC")))))
                .filter(Restrictions.and(Restrictions.tag("userId").equal(userId)))
                .toString();
        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(temperatureByLocationQuery, Data.class);
    }

    public double getTotalPowerUsed(GetTotalPowerUsedDto dto) {
        Hub hub = hubService.getHubByToken(dto.getPostToken());
        if (hub == null) {
            throw new NotAllowedException("Wrong token!");
        }
        Outlet outlet = outletService.getOutlet(dto.getOutletIdentifier(), hub.getId());

        String dataByTimeQuery = Flux.from(bucketName)
                .range(DATA_RETENTION_DAYS, ChronoUnit.DAYS)
                .filter(Restrictions.and(Restrictions.tag("outletId").equal(outlet.getId())))
                .last("totalPowerUsed")
                .toString();
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<Double> results = queryApi.query(dataByTimeQuery, Double.class);
        return results.isEmpty() ? 0 : results.get(0);
    }
}
