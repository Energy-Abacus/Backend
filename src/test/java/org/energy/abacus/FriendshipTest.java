package org.energy.abacus;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.quarkus.test.security.oidc.UserInfo;
import org.energy.abacus.dtos.FriendshipDto;
import org.energy.abacus.dtos.FriendshipReactionDto;
import org.energy.abacus.dtos.UserDto;
import org.energy.abacus.dtos.UserFriendDto;
import org.energy.abacus.entities.Friendship;
import org.energy.abacus.logic.FriendshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.wildfly.common.Assert.assertTrue;

@QuarkusTest
class FriendshipTest {

    @Inject
    FriendshipService friendshipService;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    @Transactional
    void setup() {
        entityManager.createQuery("DELETE FROM Friendship").executeUpdate();
    }

    @Test
    @TestSecurity(user = "test1", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|1")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|1"),
    })
    void testGetEmptyFriendshipList() {
        FriendshipDto[] friends = given().get("/api/v1/friendship")
                .then()
                .statusCode(200)
                .extract().body().as(FriendshipDto[].class);

        assertEquals(0, friends.length);
    }

    @Test
    @TestSecurity(user = "test1", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|1")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|1"),
    })
    void testAddFriendPending() {
        given()
                .header("Content-Type", "application/json")
                .body("test|2")
                .post("/api/v1/friendship")
                .then()
                .statusCode(200);

        Friendship[] friends = given().get("/api/v1/friendship")
                .then()
                .statusCode(200)
                .extract().body().as(Friendship[].class);

        assertEquals(1, friends.length);
        assertEquals("test|1", friends[0].getRequestSenderId());
        assertEquals("test|2", friends[0].getRequestReceiverId());
        assertFalse(friends[0].isAccepted());
    }

    @Test
    @TestSecurity(user = "test1", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|1")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|1"),
    })
    void testAddFriendAccept() {
        given()
                .header("Content-Type", "application/json")
                .body("test|2")
                .post("/api/v1/friendship")
                .then()
                .statusCode(200);

        friendshipService.reactionByReceiver(new FriendshipReactionDto("test|1", true), "test|2");
        Friendship[] friends = given().get("/api/v1/friendship")
                .then()
                .statusCode(200)
                .extract().body().as(Friendship[].class);

        assertEquals(1, friends.length);
        assertEquals("test|1", friends[0].getRequestSenderId());
        assertEquals("test|2", friends[0].getRequestReceiverId());
        assertTrue(friends[0].isAccepted());
    }

    @Test
    @TestSecurity(user = "test1", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|1")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|1"),
    })
    void testAddFriendDeny() {
        given()
                .header("Content-Type", "application/json")
                .body("test|2")
                .post("/api/v1/friendship")
                .then()
                .statusCode(200);

        friendshipService.reactionByReceiver(new FriendshipReactionDto("test|1", false), "test|2");
        Friendship[] friends = given().get("/api/v1/friendship")
                .then()
                .statusCode(200)
                .extract().body().as(Friendship[].class);

        assertEquals(0, friends.length);
    }

    @Test
    @TestSecurity(user = "test1", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|1")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|1"),
    })
    void testIsFriend() {
        given()
                .header("Content-Type", "application/json")
                .body("test|2")
                .post("/api/v1/friendship")
                .then()
                .statusCode(200);

        friendshipService.reactionByReceiver(new FriendshipReactionDto("test|1", true), "test|2");
        Friendship[] friends = given().get("/api/v1/friendship")
                .then()
                .statusCode(200)
                .extract().body().as(Friendship[].class);

        assertEquals(1, friends.length);
        assertTrue(friendshipService.isFriend("test|1", "test|2"));
        assertTrue(friendshipService.isFriend("test|2", "test|1"));
        assertFalse(friendshipService.isFriend("test|3", "test|1"));
        assertFalse(friendshipService.isFriend("test|2", "test|3"));
        assertFalse(friendshipService.isFriend("test|5", "test|10"));
    }

    @Test
    @TestSecurity(user = "test1", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|1")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|1"),
    })
    void testSearch() {
        UserDto[] users = given().get("/api/v1/friendship/search?username=testingnodelete")
                .then()
                .statusCode(200)
                .extract().body().as(UserDto[].class);

        assertTrue(users.length > 0);
        assertEquals("auth0|648f2a5f8b85c8a6949f4b74", users[0].getUser_id());
        assertEquals("testingnodelete", users[0].getUsername());
    }

    @Test
    @TestSecurity(user = "test1", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|1")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|1"),
    })
    void testFriendDetails() {
        given()
                .header("Content-Type", "application/json")
                .body("auth0|648f2a5f8b85c8a6949f4b74")
                .post("/api/v1/friendship")
                .then()
                .statusCode(200);

        friendshipService.reactionByReceiver(new FriendshipReactionDto("test|1", true), "auth0|648f2a5f8b85c8a6949f4b74");

        UserFriendDto[] users = given().get("/api/v1/friendship/friend-details")
                .then()
                .statusCode(200)
                .extract().body().as(UserFriendDto[].class);

        assertEquals(1, users.length);
        assertEquals("auth0|648f2a5f8b85c8a6949f4b74", users[0].getUserId());
        assertEquals("testingnodelete", users[0].getUsername());
        assertTrue(users[0].isAccepted());
        assertTrue(users[0].isOutgoing());

    }
}