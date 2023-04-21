package org.energy.abacus.ressource;

import io.quarkus.security.Authenticated;
import lombok.extern.java.Log;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.energy.abacus.dtos.OutletDto;
import org.energy.abacus.entities.Outlet;
import org.energy.abacus.logic.OutletService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@Path("/api/v1/outlet")
@RequestScoped
@Log
public class OutletResource {

    @Inject
    OutletService outletService;

    @Inject
    @Claim(standard = Claims.sub)
    String userId;

    @POST
    @Authenticated
    @Transactional
    public int saveOutlet(final OutletDto outlet) {
        return outletService.addNewOutlet(outlet, userId);
    }

    @GET
    @Authenticated
    public Collection<Outlet> getAllOutlets(@QueryParam("hubId") int hubId) {
        return outletService.getAllOutletsForHub(hubId, userId);
    }
}
