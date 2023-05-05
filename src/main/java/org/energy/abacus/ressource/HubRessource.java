package org.energy.abacus.ressource;

import io.quarkus.security.Authenticated;
import lombok.extern.java.Log;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.energy.abacus.dtos.HubDto;
import org.energy.abacus.entities.Hub;
import org.energy.abacus.logic.HubService;
import org.energy.abacus.logic.MeasurementService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@Path("/api/v1/hub")
@RequestScoped
@Log
public class HubRessource {

    @Inject
    HubService hubService;

    @Inject
    @Claim(standard = Claims.sub)
    String userId;

    @POST
    @Authenticated
    @Transactional
    public Hub saveHub(final HubDto hubDto) {
        return hubService.addNewHub(hubDto, userId);
    }

    @GET
    @Authenticated
    public Collection<Hub> getAllHubs() {
        return hubService.getAllHubsForUser(userId);
    }

    @GET
    @Authenticated
    @Path("{id}")
    public Hub getHub(@PathParam("id") int id) {
        return hubService.getHubById(id, userId);
    }
}
