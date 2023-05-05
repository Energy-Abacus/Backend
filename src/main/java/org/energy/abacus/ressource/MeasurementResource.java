package org.energy.abacus.ressource;

import lombok.extern.java.Log;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.energy.abacus.dtos.GetTotalPowerUsedDto;
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
    @Path("/total")
    public double getTotalPowerUsed(final GetTotalPowerUsedDto dto) {
        return measurementService.getTotalPowerUsed(dto);
    }

    @GET
    public Collection<Data> getMeasurementsByOutletId(
            @QueryParam("outletId") int outletId,
            @QueryParam("from") long from,
            @QueryParam("to") long to
    ) {
        return measurementService.getMeasurementsByOutletInTimeFrame(outletId, from, to, userId);
    }
}