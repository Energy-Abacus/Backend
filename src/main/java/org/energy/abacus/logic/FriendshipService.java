package org.energy.abacus.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.energy.abacus.dtos.FriendshipDto;
import org.energy.abacus.dtos.UserDto;
import org.energy.abacus.entities.Friendship;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.regex.Pattern;

@ApplicationScoped
@Log
public class FriendshipService {
    @Inject
    EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    String domain = "https://dev-3adocs3bconafo8d.us.auth0.com/";
    String clientId = "K97VVGMJaYDn5Z0Qw9tx7NaJXTeQrGZ0";
    String clientSecret = "RGhYxlKdeQ6HTkwUaGZTBNjiV5Y2vRpZz4MWTFNhHnuzM2-7zR27pLCwNjMOsht3";
    String audience = "https://dev-3adocs3bconafo8d.us.auth0.com/api/v2/";
    String token;

    public FriendshipService() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(domain + "oauth/token"))
                .POST(HttpRequest.BodyPublishers.ofString("&audience=" + audience + "&client_id=" + clientId + "&client_secret=" + clientSecret
                        + "&grant_type=client_credentials"))
                .build();
        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        final ObjectNode node = objectMapper.readValue(response.body(), ObjectNode.class);
        token = node.get("access_token").asText();

    }

    public int addNewFriend(String receiver, String sender){
        Friendship friendshipEntity = Friendship.builder()
                .requestSenderId(sender)
                .requestReceiverId(receiver)
                .accepted(false)
                .build();
        entityManager.persist(friendshipEntity);
        return friendshipEntity.getId();
    }

    public int reactionByReceiver(boolean requestReaction, String receiver, String sender){
        if(requestReaction) {
            entityManager.createNamedQuery("updateFriendshipByUsers", Friendship.class)
                    .setParameter("reaction", true)
                    .setParameter("sender", sender)
                    .setParameter("receiver", receiver)
                    .getSingleResult();
            return 1;
        }
        else{
            entityManager.createNamedQuery("deleteFriendshipByUsers", Friendship.class)
                    .setParameter("sender", sender)
                    .setParameter("receiver", receiver)
                    .getSingleResult();
            return 0;

        }

    }

    public Collection<Friendship> getFriendshipList(String receiver){
        return entityManager.createNamedQuery("findFriendshipUsers",Friendship.class)
                .setParameter("receiver",receiver)
                .getResultList();
    }

    public Collection<UserDto> getAllUsersByName(String chars) {

        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_.-.!.#.$.^.~.@]*$");

        if(!pattern.matcher(chars).matches()){
            throw new BadRequestException("Illegal characters in username");
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .headers("authorization", "Bearer " + token)
                .uri(URI.create(domain + "api/v2/users?search_engine=v3&q=username:*" + chars + "*"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            UserDto[] users = objectMapper.readValue(response.body(), UserDto[].class);
            return Arrays.asList(users);
        } catch (IOException | InterruptedException e) {
            log.log(Level.SEVERE, "Error while getting users from Auth0", e);
            Thread.currentThread().interrupt();
            throw new InternalServerErrorException("Error while getting users from Auth0");
        }
    }
}
