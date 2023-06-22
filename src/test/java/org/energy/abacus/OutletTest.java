package org.energy.abacus;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.quarkus.test.security.oidc.UserInfo;
import org.energy.abacus.entities.Hub;
import org.energy.abacus.entities.Outlet;
import org.energy.abacus.logic.OutletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wildfly.common.Assert.assertFalse;

@QuarkusTest
class OutletTest {

    @Inject
    EntityManager entityManager;

    @Inject
    OutletService outletService;

    @BeforeEach
    @Transactional
    void setup() {
        entityManager.createQuery("DELETE FROM Outlet").executeUpdate();
        entityManager.createQuery("DELETE FROM Hub").executeUpdate();
    }

    @Test
    @TestSecurity(user = "user", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|123")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|123"),
    })
    void testGetOutletById() {
        Hub hub = given().body("{\"name\": \"Test Hub\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/hub")
                .then()
                .statusCode(200)
                .extract().body().as(Hub.class);

        int outletId = given().body("{\"name\": \"Test Outlet\", \"outletIdentifier\": \"shelly-1234\", \"hubId\": \"" + hub.getId() + "\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/outlet")
                .then().statusCode(200).extract().body().as(Integer.class);

        Outlet outlet = given()
                .get("/api/v1/outlet/" + outletId)
                .then()
                .statusCode(200)
                .extract().body().as(Outlet.class);

        assertEquals(outletId, outlet.getId());
        assertEquals("Test Outlet", outlet.getName());
        assertEquals("shelly-1234", outlet.getOutletIdentifier());
        assertEquals(hub.getId(), outlet.getHubId());
    }

    @Test
    @TestSecurity(user = "user", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|123")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|123"),
    })
    void testGetAllOutletsForHub() {
        Hub hub = given().body("{\"name\": \"Test Hub\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/hub")
                .then()
                .statusCode(200)
                .extract().body().as(Hub.class);

        int outletId = given().body("{\"name\": \"Test Outlet\", \"outletIdentifier\": \"shelly-1234\", \"hubId\": \"" + hub.getId() + "\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/outlet")
                .then().statusCode(200).extract().body().as(Integer.class);

        Outlet[] outlet = given()
                .get("/api/v1/outlet?hubId=" + hub.getId())
                .then()
                .statusCode(200)
                .extract().body().as(Outlet[].class);

        assertEquals(1, outlet.length);
        assertEquals(outletId, outlet[0].getId());
        assertEquals("Test Outlet", outlet[0].getName());
        assertEquals("shelly-1234", outlet[0].getOutletIdentifier());
        assertEquals(hub.getId(), outlet[0].getHubId());
    }

    @Test
    @TestSecurity(user = "user", roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = "test|123")
    }, userinfo = {
            @UserInfo(key= "sub", value = "test|123"),
    })
    void testOutletBelongsToUser() {
        Hub hub = given().body("{\"name\": \"Test Hub\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/hub")
                .then()
                .statusCode(200)
                .extract().body().as(Hub.class);

        int outletId = given().body("{\"name\": \"Test Outlet\", \"outletIdentifier\": \"shelly-1234\", \"hubId\": \"" + hub.getId() + "\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/outlet")
                .then().statusCode(200).extract().body().as(Integer.class);

        assertTrue(outletService.outletBelongsToUser(outletId, "test|123"));
        assertFalse(outletService.outletBelongsToUser(outletId, "test|321"));
        assertFalse(outletService.outletBelongsToUser(10000, "test|123"));
    }
}
