package org.energy.abacus.resource;

import io.quarkus.security.Authenticated;
import lombok.extern.java.Log;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.energy.abacus.dtos.UserDto;
import org.energy.abacus.entities.Friendship;
import org.energy.abacus.logic.FriendshipService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Collection;

@Path("/api/v1/friendship")
@RequestScoped
@Log
public class FriendshipResource {

    @Inject
    @Claim(standard = Claims.sub)
    String userId;

    @Inject
    FriendshipService service;

    @POST
    @Authenticated
    @Transactional
    public int newRequest(String receiver) {
        return service.addNewFriend(receiver,userId);
    }

    @POST
    @Path("/reaction")
    @Authenticated
    @Transactional
    public int update(
            @QueryParam("receiver") String receiver,
            @QueryParam("accept") boolean reaction
    ) {
        return service.reactionByReceiver(reaction,receiver,userId);
    }

    @GET
    @Authenticated
    @Transactional
    public Collection<Friendship> getAll() {
        return service.getFriendshipList(userId);
    }

    @GET
    @Path("/search")
    public Collection<UserDto> searchByUserName(@QueryParam("username") String username) {
        return service.getAllUsersByName(username);
    }
}
