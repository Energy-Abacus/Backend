package org.energy.abacus;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.quarkus.test.security.oidc.UserInfo;
import org.energy.abacus.entities.Hub;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@Testcontainers
class MeasurementTest {

    final PortBinding portBinding = new PortBinding(Ports.Binding.bindPort(8086), new ExposedPort(8086));

    @Container
    final InfluxDBContainer<?> influxContainer = new InfluxDBContainer<>(DockerImageName.parse("influxdb:latest"))
            .withAdminToken("my-super-secret-auth-token")
            .withOrganization("abacustesting")
            .withBucket("dummy")
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(new HostConfig().withPortBindings(portBinding)))
            .withExposedPorts(8086);

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
                .post("/api/v1/hub")
                .then()
                    .statusCode(200)
                    .extract().body().as(Hub.class);

        int outletId = given().body("{\"name\": \"Test Outlet\", \"outletIdentifier\": \"shelly-1234\", \"hubId\": \"" + hub.getId() + "\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/outlet")
                .then().statusCode(200).extract().body().as(Integer.class);

        double totalPowerUsed = given().body("{\"outletIdentifier\": \"shelly-1234\", \"postToken\": \"" + hub.getPostToken() + "\"}")
                .header("Content-Type", "application/json")
                .when()
                .get("/api/v1/measurement/total")
                .then().statusCode(200).extract().body().as(Double.class);
        assertEquals(0.0, totalPowerUsed);

        given().body("{\"postToken\": \"" + hub.getPostToken() + "\", \"timeStamp\": 1646311120, \"powerOn\": \"true\", \"wattPower\": 0, \"totalPowerUsed\": " + (totalPowerUsed + 5.0) + ", \"temperature\": 20.0, \"outletIdentifier\": \"shelly-1234\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/measurement")
                .then().statusCode(204);

        given().when()
                .get("/api/v1/measurement?outletId="+outletId+"&from=1646310120&to=1646316000")
                .then()
                    .statusCode(200)
                    .body("$.size()", is(1),
                        "[0].temperature", is(20.0F));

        totalPowerUsed = given().body("{\"outletIdentifier\": \"shelly-1234\", \"postToken\": \"" + hub.getPostToken() + "\"}")
                .header("Content-Type", "application/json")
                .when()
                .get("/api/v1/measurement/total")
                .then().statusCode(200).extract().body().as(Double.class);

        System.out.println("Alo: " + totalPowerUsed);
        assertEquals(5.0, totalPowerUsed);

        given().body("{\"postToken\": \"" + hub.getPostToken() + "\", \"timeStamp\": 1646311150, \"powerOn\": \"true\", \"wattPower\": 0, \"totalPowerUsed\": " + (totalPowerUsed + 2.0) + ", \"temperature\": 21.0, \"outletIdentifier\": \"shelly-1234\"}")
                .header("Content-Type", "application/json")
                .when()
                .post("/api/v1/measurement")
                .then().statusCode(204);

        totalPowerUsed = given().body("{\"outletIdentifier\": \"shelly-1234\", \"postToken\": \"" + hub.getPostToken() + "\"}")
                .header("Content-Type", "application/json")
                .when()
                .get("/api/v1/measurement/total")
                .then().statusCode(200).extract().body().as(Double.class);

        assertEquals(7.0, totalPowerUsed);
    }
}