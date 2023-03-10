package org.energy.abacus.ressource;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.energy.abacus.dtos.HubDto;
import org.energy.abacus.dtos.MeasurementDto;
import org.energy.abacus.dtos.OutletDto;
import org.energy.abacus.entities.Hub;
import org.energy.abacus.entities.Measurement;
import org.energy.abacus.entities.Outlet;
import org.energy.abacus.logic.MeasurementService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

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
    @Transactional
    public void save(final MeasurementDto measurement) {
        measurementService.addNewMeasurement(measurement);
    }

    @GET
    @Authenticated
    public Collection<Measurement> getMeasurementsByOutletId(@QueryParam("outletId") int outletId) {
        return measurementService.getMeasurementsByOutlet(outletId, userId);
    }

    @POST
    @Authenticated
    @Transactional
    @Path("/outlet")
    public int saveOutlet(final OutletDto outlet) {
        return measurementService.addNewOutlet(outlet, userId);
    }

    @GET
    @Authenticated
    @Path("/outlet")
    public Collection<Outlet> getAllOutlets(@QueryParam("hubId") int hubId) {
        return measurementService.getAllOutletsForHub(hubId, userId);
    }

    @POST
    @Authenticated
    @Transactional
    @Path("/hub")
    public Hub saveHub(final HubDto hubDto) {
        return measurementService.addNewHub(hubDto, userId);
    }

    @GET
    @Authenticated
    @Path("/hub")
    public Collection<Hub> getAllHubs() {
        return measurementService.getAllHubsForUser(userId);
    }

    @GET
    @Authenticated
    @Path("/hub/{id}")
    public Hub getHub(@PathParam("id") int id) {
        return measurementService.getHubById(id, userId);
    }
}