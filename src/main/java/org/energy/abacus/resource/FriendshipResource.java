package org.energy.abacus.resource;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.energy.abacus.dtos.FriendshipReactionDto;
import org.energy.abacus.dtos.UserDto;
import org.energy.abacus.dtos.UserFriendDto;
import org.energy.abacus.entities.Friendship;
import org.energy.abacus.logic.FriendshipService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import java.util.Collection;

@Path("/api/v1/friendship")
@Produces("application/json")
@Consumes("application/json")
@RequestScoped
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
    public int update(FriendshipReactionDto dto) {
        return service.reactionByReceiver(dto, userId);
    }

    @DELETE
    @Path("/{id}")
    @Authenticated
    @Transactional
    public int delete(@PathParam("id") int id) {
        return service.deleteFriendship(id, userId);
    }

    @GET
    @Authenticated
    @Transactional
    public Collection<Friendship> getAll() {
        return service.getFriendshipList(userId);
    }

    @GET
    @Authenticated
    @Path("/friend-details")
    public Collection<UserFriendDto> getAllUserProfiles() {
        return service.getAllUserProfiles(userId);
    }

    @GET
    @Path("/search")
    @Authenticated
    public Collection<UserDto> searchByUserName(@QueryParam("username") String username) {
        return service.getAllUsersByName(username);
    }
}
