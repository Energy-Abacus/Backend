package org.energy.abacus.resource;

import io.quarkus.security.Authenticated;
import org.energy.abacus.entities.DeviceType;
import org.energy.abacus.logic.DeviceTypeService;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.Collection;

@Path("/api/v1/device-type")
@Produces("application/json")
@Consumes("application/json")
public class DeviceTypeResource {

    @Inject
    DeviceTypeService service;

    @Authenticated
    @GET
    public Collection<DeviceType> getAll(){return service.getDeviceTypeList();}

}
