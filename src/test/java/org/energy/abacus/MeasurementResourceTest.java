package org.energy.abacus;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class MeasurementResourceTest {

    @Test
    public void testNewMeasurement(){
        given().body("{\"power\": 20}")
                .header("Content-Type", "application/json")
                .when()
                .post("api/v1/Measurements")
                .then();

        given().when()
                .get("api/v1/Measurements")
                .then()
                .body("$.size()", is(1),
                        "[0].power", is(20.0F));
    }
}