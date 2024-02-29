package org.energy.abacus.logic;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxTable;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.RangeFlux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.energy.abacus.dtos.GetSumTotalPowerDto;
import org.energy.abacus.dtos.GetSumWattDto;
import org.energy.abacus.dtos.GetTotalPowerUsedDto;
import org.energy.abacus.dtos.MeasurementDto;
import org.energy.abacus.entities.Data;
import org.energy.abacus.entities.Hub;
import org.energy.abacus.entities.Outlet;
import org.jboss.resteasy.reactive.common.NotImplementedYet;
import org.w3c.dom.ranges.Range;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.NotAllowedException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;

@ApplicationScoped
@Log
public class MeasurementService {

    @Inject
    EntityManager entityManager;

    @Inject
    HubService hubService;

    @Inject
    FriendshipService friendshipService;
    @Inject
    OutletService outletService;

    private InfluxDBClient influxDBClient;
    @ConfigProperty(name = "influxdb.url")
    String connectionUrl;
    @ConfigProperty(name = "influxdb.token")
    String token;
    @ConfigProperty(name = "influxdb.org")
    String orgId;
    @ConfigProperty(name = "influxdb.bucket")
    String bucketId;
    @ConfigProperty(name = "influxdb.bucket")
    String bucketName;
    @ConfigProperty(name = "influxdb.loglevel", defaultValue = "NONE")
    LogLevel logLevel;

    public static final Long DATA_RETENTION_DAYS = -1095L;

    @PostConstruct
    public void initializeInfluxDBClient() {
        log.log(Level.INFO, String.format("Connecting to: %s, token: %s, org: %s, bucketId: %s",
                connectionUrl, token, orgId, bucketId));
        this.influxDBClient = InfluxDBClientFactory.create(connectionUrl, token.toCharArray(), orgId, bucketId);
        this.influxDBClient.setLogLevel(logLevel);
    }

    public void addNewMeasurement(final MeasurementDto measurementDto) {
        Hub hub = hubService.getHubByToken(measurementDto.getPostToken());

        if (hub == null) {
            throw new NotAllowedException("Wrong token!");
        }

        Outlet outlet = outletService.getOutlet(measurementDto.getOutletIdentifier(), hub.getId());

        Data data = Data.builder()
                        .timeStamp(Instant.ofEpochSecond(Long.parseLong(measurementDto.getTimeStamp())))
                        .wattPower(Double.parseDouble(measurementDto.getWattPower()))
                        .totalPowerUsed(Double.parseDouble(measurementDto.getTotalPowerUsed()))
                        .temperature(Double.parseDouble(measurementDto.getTemperature()))
                        .outletId(Integer.toString(outlet.getId()))
                        .build();

        try (WriteApi writeApi = influxDBClient.makeWriteApi()) {
            writeApi.writeMeasurement(WritePrecision.S, data);
        }
    }

    public List<Data> getMeasurementsByOutletInTimeFrame(int outletId, long from, long to, String userId) {

        if (!outletService.outletBelongsToUser(outletId, userId)) {
            throw new NotAllowedException("Outlet doesn't exist or you don't have access to it!");
        }

        String measurementsInTimeframeQuery = Flux.from(bucketName)
                .range(Instant.ofEpochSecond(from), Instant.ofEpochSecond(to))
                .filter(Restrictions.and(Restrictions.tag("outletId").equal(Integer.toString(outletId))))
                .pivot(new String[] { "_time" }, new String[] { "_field" }, "_value")
                .toString();
        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(measurementsInTimeframeQuery, Data.class);
    }

    public List<Data> getMeasurementsOfFriend(int outletId, long from, long to, String userId, String friendId){
        if(!friendshipService.isFriend(userId,friendId)){
            throw new NotAllowedException("The requested user is not your friend!");
        }

        return getMeasurementsByOutletInTimeFrame(outletId,from,to,friendId);
    }

    public double getAverageActivePowerUsedByOutlet(int outletId, String userId) {
        if (!outletService.outletBelongsToUser(outletId, userId)) {
            throw new NotAllowedException("Outlet doesn't exist or you don't have access to it!");
        }

        return getTotalPowerUsedFilteredByOutlet(outletId);
    }

    public double getAveragePowerUsedByOutlet(int outletId, String userId) {
        if (!outletService.outletBelongsToUser(outletId, userId)) {
            throw new NotAllowedException("Outlet doesn't exist or you don't have access to it!");
        }
        return getTotalPowerUsedByOutlet(outletId) / countMeasurements(Flux.from(bucketName).range(DATA_RETENTION_DAYS, ChronoUnit.DAYS), outletId, "totalPowerUsed");
    }

    public double getAveragePowerUsedByOutletBetween(int outletId, String userId, long from, long to) {
        if (!outletService.outletBelongsToUser(outletId, userId)) {
            throw new NotAllowedException("Outlet doesn't exist or you don't have access to it!");
        }
        RangeFlux rangeFlux = Flux.from(bucketName).range(
                Instant.ofEpochSecond(from), Instant.ofEpochSecond(to)
        );
        double totalPowerUsed = getTotalPowerUsedByOutletBetween(from, to, outletId);
        return totalPowerUsed / countMeasurements(rangeFlux, outletId, "totalPowerUsed");
    }

    private long countMeasurements(RangeFlux flux, int outletId, String field) {
        String countMeasurementsQuery = flux
                .filter(Restrictions.and(Restrictions.tag("outletId").equal(Integer.toString(outletId))))
                .filter(Restrictions.and(Restrictions.field().equal(field)))
                .drop(new String[] { "_start", "_stop", "_field", "_measurement", "_time", "outletId" })
                .count()
                .toString();

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> results = queryApi.query(countMeasurementsQuery);
        return results.isEmpty() ? 0 : (long) results.get(0).getRecords().get(0).getValueByKey("_value");
    }

    public double getTotalPowerUsedByOutletWithPostToken(GetTotalPowerUsedDto dto) {
        Hub hub = hubService.getHubByToken(dto.getPostToken());
        if (hub == null) {
            throw new NotAllowedException("Wrong token!");
        }
        Outlet outlet = outletService.getOutlet(dto.getOutletIdentifier(), hub.getId());
        return getTotalPowerUsedByOutlet(outlet.getId());
    }

    public double getTotalPowerUsedByOutletWithUserId(int outletId, String userId) {
        if (!outletService.outletBelongsToUser(outletId, userId)) {
            throw new NotAllowedException("Outlet doesn't exist or you don't have access to it!");
        }
        return getTotalPowerUsedByOutlet(outletId);
    }

    private double getTotalPowerUsedByOutletBetween(long start, long end, int outletId) {
        double totalPowerStart = this.getTotalPowerUsedByOutlet(Flux.from(bucketName).range(
                Instant.now().minus(DATA_RETENTION_DAYS, ChronoUnit.DAYS), Instant.ofEpochSecond(start)
        ), outletId);
        double totalPowerEnd = this.getTotalPowerUsedByOutlet(Flux.from(bucketName).range(
                Instant.now().minus(DATA_RETENTION_DAYS, ChronoUnit.DAYS), Instant.ofEpochSecond(end)
        ), outletId);
        return totalPowerEnd - totalPowerStart;
    }

    /**
     * Returns the total power used by an outlet
     * DOES NOT AUTHENTICATE THE USER - USE WITH CAUTION
     * @param outletId the id of the outlet
     * @return the total power used by the outlet
     */
    private double getTotalPowerUsedByOutlet(int outletId) {
        return this.getTotalPowerUsedByOutlet(Flux.from(bucketName).range(DATA_RETENTION_DAYS, ChronoUnit.DAYS), outletId);
    }

    /**
     * Returns the total power used by an outlet
     * DOES NOT AUTHENTICATE THE USER - USE WITH CAUTION
     * @param flux RangeFlux specifying the time range
     * @param outletId the id of the outlet
     * @return the total power used by the outlet in the given timeframe
     */
    private double  getTotalPowerUsedByOutlet(RangeFlux flux, int outletId) {
        String totalPowerUsedQuery = flux
                .filter(Restrictions.and(Restrictions.tag("outletId").equal(Integer.toString(outletId))))
                .filter(Restrictions.and(Restrictions.field().equal("totalPowerUsed")))
                .drop(new String[]{"_start", "_stop", "_field", "_measurement", "_time", "outletId"})
                .last()
                .toString();
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> results = queryApi.query(totalPowerUsedQuery);
        return results.isEmpty() ? 0 : (double) results.get(0).getRecords().get(0).getValueByKey("_value");
    }

    public double getTotalPowerUsedByUser(String userId) {
        return this.getTotalPowerUsedByUser(Flux.from(bucketName).range(DATA_RETENTION_DAYS, ChronoUnit.DAYS), userId);
    }

    public double getTotalPowerUsedByUserBetween(long start, long end, String userId) {
        double totalPowerStart = this.getTotalPowerUsedByUser(Flux.from(bucketName).range(
            Instant.now().minus(DATA_RETENTION_DAYS, ChronoUnit.DAYS), Instant.ofEpochSecond(start)
        ), userId);
        double totalPowerEnd = this.getTotalPowerUsedByUser(Flux.from(bucketName).range(
            Instant.now().minus(DATA_RETENTION_DAYS, ChronoUnit.DAYS), Instant.ofEpochSecond(end)
        ), userId);
        return totalPowerEnd - totalPowerStart;
    }

    private double getTotalPowerUsedByUser(RangeFlux rangeFlux, String userId) {
        QueryApi queryApi = influxDBClient.getQueryApi();
        String[] outletIds = outletService.getOutletIdsByUser(userId).stream().map(String::valueOf).toArray(String[]::new);

        String totalPowerByUserQuery = rangeFlux
                .filter(Restrictions.and(Restrictions.column("_field").equal("totalPowerUsed")))
                .filter(Restrictions.and(Restrictions.tag("outletId").contains(outletIds)))
                .groupBy("outletId")
                .last()
                .drop(new String[] { "_start", "_stop", "_field", "_measurement", "_time", "outletId" })
                .toString();

        double totalPowerUsed = 0;
        for (var tables : queryApi.query(totalPowerByUserQuery)) {
            for (var result : tables.getRecords()) {
                log.log(Level.SEVERE, result.getValueByKey("_value").toString());
                totalPowerUsed += (double) result.getValueByKey("_value");
            }
        }

        return totalPowerUsed;
    }

    /**
     * Returns the total power used excluding standby by an outlet
     * DOES NOT AUTHENTICATE THE USER - USE WITH CAUTION
     * @param outletId the id of the outlet
     * @return the total power used by the outlet in the given timeframe
     */
    private double getTotalPowerUsedFilteredByOutlet(int outletId){
        return this.getTotalPowerUsedFilteredByOutlet(Flux.from(bucketName).range(DATA_RETENTION_DAYS, ChronoUnit.DAYS), outletId);
    }

    /**
     * Returns the total power used excluding standby by an outlet
     * DOES NOT AUTHENTICATE THE USER - USE WITH CAUTION
     * @param flux RangeFlux specifying the time range
     * @param outletId the id of the outlet
     * @return the total power used by the outlet in the given timeframe
     */
    private double getTotalPowerUsedFilteredByOutlet(RangeFlux flux, int outletId) {
        /*
        What it should look like
        from(bucket:"measurement")
	|> range(start:-1095d)
    |> filter(fn: (r) => (r["outletId"] == "7"))
    |> filter(fn: (r) => (r["_field"] == "wattPower"))
    |> filter(fn: (r) => (r["_value"] > 2))
    |> mean(column: "_value")
    |> drop(columns:["_start", "_stop", "_field", "_measurement", "_time", "outletId"])

    What it does look like
    from(bucket:"measurement")
	|> range(start:-1095d)
	|> filter(fn: (r) => (r["_value"] > "46.428"))
	|> mean(column:"_value")
         */

        // Tipp: use new .filter for every restriction

        String measurementsInTimeframeQuery = flux
                .filter(Restrictions.and(Restrictions.tag("outletId").equal(Integer.toString(outletId))))
                .filter(Restrictions.and(Restrictions.field().equal("wattPower")))
                .filter(Restrictions.and(Restrictions.value().greater(Double.toString(getStandByPower(flux, outletId)))))
                .mean("_value")
                .toString();
        log.log(Level.SEVERE, measurementsInTimeframeQuery);
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> results = queryApi.query(measurementsInTimeframeQuery);
        return results.isEmpty() ? 0 : (double) results.get(0).getRecords().get(0).getValueByKey("_value");
    }

    public double getEstimatedStandbyPower(int outletId, String userId) {
        if (!outletService.outletBelongsToUser(outletId, userId)) {
            throw new NotAllowedException("Outlet doesn't exist or you don't have access to it!");
        }
        return getStandByPower(Flux.from(bucketName).range(DATA_RETENTION_DAYS, ChronoUnit.DAYS), outletId);
    }

    private double getStandByPower(RangeFlux flux, int outletId) {
        /*
        What it should look like:
        from(bucket:"measurement")
	|> range(start:-1095d)
	|> filter(fn: (r) => (r["_field"] == "wattPower"))
    |> filter(fn: (r) => (r["outletId"] == "7"))
	|> drop(columns:["_start", "_stop", "_field", "_measurement", "_time", "outletId"])
	|> max()

        What it does look like:
         from(bucket:"measurement")
	|> range(start:-1095d)
	|> filter(fn: (r) => (r["_field"] == "wattPower"))
	|> drop(columns:["_start", "_stop", "_field", "_measurement", "_time", "outletId"])
	|> max()
         */

        String measurementsInTimeframeQuery = flux
                .filter(Restrictions.and(Restrictions.tag("outletId").equal(Integer.toString(outletId))))
                .filter(Restrictions.and(Restrictions.field().equal("wattPower")))
                .drop(new String[] { "_start", "_stop", "_field", "_measurement", "_time", "outletId" })
                .max()
                .toString();
        QueryApi queryApi = influxDBClient.getQueryApi();
        log.log(Level.SEVERE, measurementsInTimeframeQuery);
        List<FluxTable> results = queryApi.query(measurementsInTimeframeQuery);
        return results.isEmpty() ? 0 : (((double) results.get(0).getRecords().get(0).getValueByKey("_value")) * 0.3);
    }

    public List<GetSumWattDto> getSumWattByUser(long start, long end, String userId) {
        String[] outletIds = outletService.getOutletIdsByUser(userId).stream().map(String::valueOf).toArray(String[]::new);

        /*
        from(bucket: "measurement")
          |> range(start: -100d, stop: -99d)
          |> filter(fn: (r) => (r["_field"] == "totalPowerUsed"))
          |> filter(fn: (r) => (contains(value: r["outletId"], set:["11", "12"])))
          |> aggregateWindow(every: 15m, fn: mean, column: "_value")
          |> pivot(rowKey: ["_time"], columnKey: ["outletId"], valueColumn: "_value")
          |> map(fn: (r) => ({
            _time: r["_time"],
            _value: (if exists r["11"] then r["11"] else 0.0) + (if exists r["12"] then r["12"] else 0.0)
            }))
         */

        StringBuilder addPlugValuesBuilder = new StringBuilder();

        for (String outletId : outletIds) {
            addPlugValuesBuilder.append("(if exists r[\"").append(outletId).append("\"] then r[\"").append(outletId).append("\"] else 0.0) + ");
        }
        addPlugValuesBuilder.delete(addPlugValuesBuilder.length() - 3, addPlugValuesBuilder.length());

        String totalPowerByUserQuery = Flux.from(bucketName)
                .range(Instant.ofEpochSecond(start), Instant.ofEpochSecond(end))
                .filter(Restrictions.and(Restrictions.column("_field").equal("wattPower")))
                .filter(Restrictions.and(Restrictions.tag("outletId").contains(outletIds)))
                .expression("aggregateWindow(every: 15m, fn: mean, column: \"_value\")")
                .pivot(new String[] { "_time" }, new String[] { "outletId" }, "_value")
                .map(String.format("({ _time: r[\"_time\"], _wattPowerSum: (%s) })", addPlugValuesBuilder.toString()))
                .toString();

        QueryApi queryApi = influxDBClient.getQueryApi();
        log.log(Level.SEVERE, totalPowerByUserQuery);
        return queryApi.query(totalPowerByUserQuery, GetSumWattDto.class);
    }

    public List<GetSumTotalPowerDto> getSumTotalPower(long start, long end, String userId) {
        String[] outletIds = outletService.getOutletIdsByUser(userId).stream().map(String::valueOf).toArray(String[]::new);

        StringBuilder addPlugValuesBuilder = new StringBuilder();

        for (String outletId : outletIds) {
            addPlugValuesBuilder.append("(if exists r[\"").append(outletId).append("\"] then r[\"").append(outletId).append("\"] else 0.0) + ");
        }
        addPlugValuesBuilder.delete(addPlugValuesBuilder.length() - 3, addPlugValuesBuilder.length());

        String totalPowerByUserQuery = Flux.from(bucketName)
                .range(Instant.ofEpochSecond(start), Instant.ofEpochSecond(end))
                .filter(Restrictions.and(Restrictions.column("_field").equal("totalPowerUsed")))
                .filter(Restrictions.and(Restrictions.tag("outletId").contains(outletIds)))
                .expression("aggregateWindow(every: 15m, fn: mean, column: \"_value\")")
                .pivot(new String[] { "_time" }, new String[] { "outletId" }, "_value")
                .map(String.format("({ _time: r[\"_time\"], _totalPowerUsedSum: (%s) })", addPlugValuesBuilder.toString()))
                .toString();

        QueryApi queryApi = influxDBClient.getQueryApi();
        log.log(Level.SEVERE, totalPowerByUserQuery);
        return queryApi.query(totalPowerByUserQuery, GetSumTotalPowerDto.class);
    }
}
