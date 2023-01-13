package org.energy.abacus.ressource;

import org.energy.abacus.entities.Measurement;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@Path("/api/v1/Measurements")
@ApplicationScoped
public class MeasurementResource{

    @POST
    @Transactional
    public void save(final Measurement measurement) {
        measurement.persist();
    }

    @GET
    public Collection<Measurement> getAllMeasurements() {
        return Measurement.listAll();
    }
}
