package org.energy.abacus;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class OutletResourceTest{

    @Test
    public void testNewOutlet() {
        given().body("{\"name\": \"Outlet1\", \"powerOn\":true}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/Outlets")
                .then();

        given().when()
                .get("/api/v1/Outlets")
                .then()
                .body("$.size()", is(1),
                        "[0].name", is("Outlet1"));
    }
}
