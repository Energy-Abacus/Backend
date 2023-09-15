package org.energy.abacus.resource;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.energy.abacus.entities.DeviceType;
import org.energy.abacus.logic.DeviceTypeService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;

@Path("/api/v1/device-type")
@Produces("application/json")
@Consumes("application/json")
public class DeviceTypeResource {

    @Inject
    @Claim(standard = Claims.sub)
    String userId;

    @Inject
    DeviceTypeService service;
    @GET
    public Collection<DeviceType> getAll(){return service.getDeviceTypeList();}
}
