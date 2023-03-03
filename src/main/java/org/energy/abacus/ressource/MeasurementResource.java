package org.energy.abacus.ressource;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.energy.abacus.dtos.MeasurementDto;
import org.energy.abacus.dtos.OutletDTO;
import org.energy.abacus.entities.Measurement;
import org.energy.abacus.entities.Outlet;
import org.energy.abacus.logic.MeasurementService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@Path("/api/v1/measurements")
@RequestScoped
public class MeasurementResource{

    @Inject
    MeasurementService measurementService;

    @Inject
    @Claim(standard = Claims.sub)
    String userId;

    @POST
    @Authenticated
    @Transactional
    public void save(final MeasurementDto measurement) {
        measurementService.addNewMeasurement(measurement, userId);
    }

    @GET
    @Authenticated
    public List<Measurement> getMeasurementsByOutletId(@QueryParam("outletId") int outletId) {
        return measurementService.getMeasurementsByOutlet(outletId);
    }

    @POST
    @Authenticated
    @Transactional
    @Path("/outlet")
    public int saveOutlet(final OutletDTO outlet) {
        return measurementService.addNewOutlet(outlet, userId);
    }

    @GET
    @Authenticated
    @Path("/outlet")
    public List<Outlet> getAllOutlets() {
        return measurementService.getAllOutletsForUser(userId);
    }
}