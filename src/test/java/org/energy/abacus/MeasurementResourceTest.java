package org.energy.abacus;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.quarkus.test.security.oidc.UserInfo;
import org.energy.abacus.entities.Hub;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class MeasurementResourceTest {

    @Test
    @TestSecurity(user = "user", roles = "user")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "test|123")
    }, userinfo = {
        @UserInfo(key= "sub", value = "test|123"),
    })
    void testNewMeasurement() {
        Hub hub = given().body("{\"name\": \"Test Hub\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/measurements/hub")
                .then()
                    .statusCode(200)
                    .extract().body().as(Hub.class);

        int outletId = given().body("{\"name\": \"Test Outlet\", \"outletIdentifier\": \"shelly-1234\", \"hubId\": \"" + hub.getId() + "\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/measurements/outlet")
                .then().statusCode(200).extract().body().as(Integer.class);

        given().body("{\"postToken\": \"" + hub.getPostToken() + "\", \"timeStamp\": \"2022/03/03 13:22:50\", \"powerOn\": \"true\", \"wattPower\": 0, \"wattMinutePower\": 0, \"temperature\": 20.0, \"outletIdentifier\": \"shelly-1234\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/measurements")
                .then().statusCode(204);

        given().when()
                .get("/api/v1/measurements?outletId=" + outletId)
                .then()
                .body("$.size()", is(1),
                        "[0].temperature", is(20.0F));
    }
}