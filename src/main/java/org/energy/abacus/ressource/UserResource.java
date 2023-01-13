package org.energy.abacus.ressource;

import org.energy.abacus.entities.User;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

@Path("/api/v1/Users")
@ApplicationScoped
public class UserResource {

    @POST
    @Transactional
    public void save(final User user) {
        user.persist();
    }

    @GET
    public Collection<User> getAllUsers(){return User.listAll();}
}
