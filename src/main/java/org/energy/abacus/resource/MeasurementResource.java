package org.energy.abacus.resource;

import io.quarkus.security.Authenticated;
import lombok.extern.java.Log;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.energy.abacus.dtos.GetTotalPowerUsedDto;
import org.energy.abacus.dtos.LeaderBoarPositionDto;
import org.energy.abacus.dtos.MeasurementDto;
import org.energy.abacus.entities.Data;
import org.energy.abacus.logic.MeasurementService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@Log
@Path("/api/v1/measurement")
@RequestScoped
public class MeasurementResource {

    @Inject
    MeasurementService measurementService;

    @Inject
    @Claim(standard = Claims.sub)
    String userId;

    @POST
    @Transactional
    public void save(final MeasurementDto measurement) {
        measurementService.addNewMeasurement(measurement);
    }

    @GET
    @Path("/total-power-plug")
    public double getTotalPowerUsed(final GetTotalPowerUsedDto dto) {
        return measurementService.getTotalPowerUsedByOutletWithPostToken(dto);
    }

    @GET
    @Path("/total-power-plug-id")
    @Authenticated
    public double getTotalPowerUsed(@QueryParam("outletId") int outletId) {
        return measurementService.getTotalPowerUsedByOutletWithUserId(outletId, this.userId);
    }

    @GET
    @Path("/avg-power-plug")
    @Authenticated
    public double getAvgPowerUsed(@QueryParam("outletId") int outletId) {
        return measurementService.getAveragePowerUsedByOutlet(outletId, this.userId);
    }

    @GET
    @Path("/standby-power")
    @Authenticated
    public double getAvgStandbyPower(@QueryParam("outletId") int outletId) {
        return measurementService.getEstimatedStandbyPower(outletId, this.userId);
    }

    @GET
    @Path("/filtered-avg-power-plug")
    @Authenticated
    public double getFilteredAvgPowerUsed(@QueryParam("outletId") int outletId) {
        return measurementService.getAverageActivePowerUsedByOutlet(outletId, this.userId);
    }

    @GET
    @Path("/avg-power-plug-between")
    @Authenticated
    public double getAvgPowerUsed(
            @QueryParam("outletId") int outletId,
            @QueryParam("from") long from,
            @QueryParam("to") long to
    ) {
        return measurementService.getAveragePowerUsedByOutletBetween(outletId, this.userId, from, to);
    }

    @GET
    @Authenticated
    @Path("/total-power-user")
    public double getTotalPowerUsedByUser() {
        return measurementService.getTotalPowerUsedByUser(this.userId);
    }

    @GET
    @Authenticated
    @Path("/total-power-device-type")
    public double getTotalPowerUsedByDeviceType(
            @QueryParam("deviceTypeId") int deviceTypeId
    ) {
        return measurementService.getTotalPowerUsedByDeviceType(deviceTypeId, this.userId);
    }

    @GET
    @Authenticated
    @Path("/total-power-device-type-between")
    public double getTotalPowerUsedByDeviceTypeBetween(
            @QueryParam("deviceTypeId") int deviceTypeId,
            @QueryParam("from") long from,
            @QueryParam("to") long to
    ) {
        return measurementService.getTotalPowerUsedByDeviceTypeBetween(deviceTypeId, this.userId, from, to);
    }

    @GET
    @Authenticated
    @Path("/device-type-leaderboard")
    public Collection<LeaderBoarPositionDto> getDeviceTypeLeaderboard(
            @QueryParam("deviceTypeId") int deviceTypeId
    ) {
        return measurementService.getDeviceTypeLeaderboard(deviceTypeId, this.userId);
    }

    @GET
    @Authenticated
    @Path("/avg-power-device-type")
    public double getAvgPowerUsedByDeviceType(
            @QueryParam("deviceTypeId") int deviceTypeId
    ) {
        return measurementService.getAveragePowerUsedByDeviceType(deviceTypeId, this.userId);
    }

    @GET
    @Authenticated
    @Path("/total-power-user-between")
    public double getTotalPowerByUserBetween(
            @QueryParam("from") long from,
            @QueryParam("to") long to
    ) {
        return measurementService.getTotalPowerUsedByUserBetween(from, to, this.userId);
    }

    @GET
    @Authenticated
    public Collection<Data> getOutletMeasurements(
            @QueryParam("outletId") int outletId,
            @QueryParam("from") long from,
            @QueryParam("to") long to
    ) {
        return measurementService.getMeasurementsByOutletInTimeFrame(outletId, from, to, userId);
    }

    @GET
    @Authenticated
    @Path("/friend")
    public Collection<Data> getOutletMeasurementsByFriend(
            @QueryParam("friendId") String friendId,
            @QueryParam("outletId") int outletId,
            @QueryParam("from") long from,
            @QueryParam("to") long to
    ) {
        return measurementService.getMeasurementsOfFriend(outletId, from, to, userId, friendId);
    }
}
