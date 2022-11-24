package org.energy.abacus;

import io.smallrye.common.constraint.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@Path("/api/v1/Outlets")
@ApplicationScoped
public class OutletResource {

    @POST
    @Transactional
    public void save(final Outlet outlet) {
        //System.out.println(outlet.isOn());
        outlet.persist();
    }

    @GET
    public Collection<Outlet> getAllOutlets(){return Outlet.listAll();}
}