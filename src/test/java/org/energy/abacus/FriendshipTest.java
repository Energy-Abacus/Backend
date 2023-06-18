package org.energy.abacus;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.quarkus.test.security.oidc.UserInfo;
import org.energy.abacus.dtos.FriendshipDto;
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

        friendshipService.reactionByReceiver(true, "test|2", "test|1");
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

        friendshipService.reactionByReceiver(false, "test|2", "test|1");
        Friendship[] friends = given().get("/api/v1/friendship")
                .then()
                .statusCode(200)
                .extract().body().as(Friendship[].class);

        assertEquals(0, friends.length);
    }

    /*
     * Test cases:
     * 1. Add friendship
     * 2. Accept friendship
     * 3. Reject friendship
     * 4. Remove friendship
     */
}