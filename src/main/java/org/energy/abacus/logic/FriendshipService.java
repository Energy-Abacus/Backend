package org.energy.abacus.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.energy.abacus.dtos.UserDto;
import org.energy.abacus.dtos.UserFriendDto;
import org.energy.abacus.entities.Friendship;
import org.jose4j.jwk.Use;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@Log
public class FriendshipService {
    @Inject
    EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ConfigProperty(name = "auth0.management-api-url")
    String domain;
    @ConfigProperty(name = "auth0.management-api-client-id")
    String clientId;
    @ConfigProperty(name = "auth0.management-api-client-secret")
    String clientSecret;
    @ConfigProperty(name = "auth0.management-api-audience")
    String audience;
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

        entityManager.createNamedQuery("deleteFriendshipByUsers", Friendship.class)
                .setParameter("sender", sender)
                .setParameter("receiver", receiver)
                .executeUpdate();
            return 0;
    }

    public Collection<Friendship> getFriendshipList(String receiver){
        return entityManager.createNamedQuery("findFriendshipUsers",Friendship.class)
                .setParameter("id",receiver)
                .getResultList();
    }

    public Collection<UserFriendDto> getAllUserProfiles(String receiver){
        Collection<Friendship> friendships = getFriendshipList(receiver);

        Stream<UserFriendDto> outgoing = friendships.stream()
                .filter(f -> !f.getRequestReceiverId().equals(receiver))
                .map(f -> getUserFriendProfile(getUserById(f.getRequestReceiverId()), true, f.isAccepted()));

        Stream<UserFriendDto> ingoing = friendships.stream()
                .filter(f -> !f.getRequestSenderId().equals(receiver))
                .map(f -> getUserFriendProfile(getUserById(f.getRequestReceiverId()), false, f.isAccepted()));

        return Stream.concat(outgoing, ingoing).toList();
    }

    private UserFriendDto getUserFriendProfile(UserDto userDto, boolean outgoing, boolean accepted){
        return UserFriendDto.builder()
                .userId(userDto.getUserId())
                .username(userDto.getUsername())
                .picture(userDto.getPicture())
                .accepted(accepted)
                .outgoing(outgoing)
                .build();
    }

    private UserDto getUserById(String userId) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .headers("authorization", "Bearer " + token)
                .uri(URI.create(domain + "api/v2/users/" + URLEncoder.encode(userId, StandardCharsets.UTF_8)))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), UserDto.class);
        } catch (IOException | InterruptedException e) {
            log.log(Level.SEVERE, "Error while getting user information from Auth0", e);
            Thread.currentThread().interrupt();
            throw new InternalServerErrorException("Error while getting user information from Auth0");
        }
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
