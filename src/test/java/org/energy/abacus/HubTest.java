package org.energy.abacus;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.quarkus.test.security.oidc.UserInfo;
import org.energy.abacus.dtos.HubDto;
import org.energy.abacus.entities.Hub;
import org.energy.abacus.logic.HubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class HubTest {

    @Inject
    EntityManager entityManager;

    @Inject
    HubService hubService;

    @BeforeEach
    @Transactional
    void setup() {
        entityManager.createQuery("DELETE FROM Hub").executeUpdate();
    }

    @Test
    @TestSecurity(user = "user", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|123")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|123"),
    })
    void testGetAllHubs() {
        Hub hub = given().body("{\"name\": \"Test Hub\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/hub")
                .then()
                .statusCode(200)
                .extract().body().as(Hub.class);

        assertEquals("Test Hub", hub.getName());

        Hub[] hubs = given().get("/api/v1/hub")
                .then()
                .statusCode(200)
                .extract().body().as(Hub[].class);

        assertEquals(1, hubs.length);
        assertEquals(hub.getId(), hubs[0].getId());
        assertEquals(hub.getName(), hubs[0].getName());
    }

    @Test
    @TestSecurity(user = "user", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|123")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|123"),
    })
    void testGetById() {
        Hub hub = given().body("{\"name\": \"Test Hub\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/hub")
                .then()
                .statusCode(200)
                .extract().body().as(Hub.class);

        assertEquals("Test Hub", hub.getName());

        Hub hubGet = given().get("/api/v1/hub/" + hub.getId())
                .then()
                .statusCode(200)
                .extract().body().as(Hub.class);

        assertEquals(hub.getId(), hubGet.getId());
        assertEquals(hub.getName(), hubGet.getName());
    }

    @Test
    @TestSecurity(user = "user", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|123")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|123"),
    })
    void testGetByIdFromOtherUser() {
        HubDto hubDto = new HubDto("Test Hub");
        Hub hubFromOtherUser = hubService.addNewHub(hubDto, "test|321");

        given().get("/api/v1/hub/" + hubFromOtherUser.getId())
                .then()
                .statusCode(405);
    }
}
