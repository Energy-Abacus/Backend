package org.energy.abacus.logic;

import lombok.extern.java.Log;
import org.energy.abacus.dtos.OutletDto;
import org.energy.abacus.entities.Hub;
import org.energy.abacus.entities.Outlet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.NotAllowedException;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
@Log
public class OutletService {

    @Inject
    EntityManager entityManager;

    @Inject
    HubService hubService;
    @Inject
    DeviceTypeService deviceTypeService;

    public int addNewOutlet(final OutletDto outletDTO, String userId) {
        Hub hub = hubService.getHubById(outletDTO.getHubId(), userId);
        Outlet outletEntity = Outlet.builder()
                .name(outletDTO.getName())
                .outletIdentifier(outletDTO.getOutletIdentifier())
                .hub(hub)
                .build();
        entityManager.persist(outletEntity);
        return outletEntity.getId();
    }

    public Outlet getOutlet(String outletIdentifier, int hubId) {
        return entityManager.createNamedQuery("findOutletByIdentifier", Outlet.class)
                .setParameter("outletIdentifier", outletIdentifier)
                .setParameter("hubId", hubId)
                .getSingleResult();
    }

    public Collection<Outlet> getAllOutletsForHub(int hubId, String userId) {
        Hub hub = hubService.getHubById(hubId, userId);
        return hub.getOutlets();
    }

    public boolean outletBelongsToUser(int outletId, String userId) {
        return !entityManager.createNamedQuery("findOutletByIdAndUser", Outlet.class)
                .setParameter("outletId", outletId)
                .setParameter("userId", userId)
                .getResultList().isEmpty();
    }

    public Outlet getOutletById(int outletId, String userId) {
        return entityManager.createNamedQuery("findOutletByIdAndUser", Outlet.class)
                .setParameter("outletId", outletId)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public Outlet updateOutlet(OutletDto outlet, int outletId, String userId) {
        Outlet oldOutlet = getOutletById(outletId, userId);

        if (oldOutlet == null) {
            throw new NotAllowedException("Outlet does not belong to user");
        }

        oldOutlet.setName(outlet.getName());
        oldOutlet.setHub(hubService.getHubById(outlet.getHubId(), userId));
        oldOutlet.setDeviceTypes(deviceTypeService.getDeviceTypesById(outlet.getDeviceTypeIds()));

        return entityManager.merge(oldOutlet);
    }

    public List<String> getOutletIdentifiersByHub(String postToken) {
        Hub hub = hubService.getHubByToken(postToken);

        if (hub == null) {
            throw new NotAllowedException("Wrong token!");
        }

        return hub.getOutlets().stream().map(Outlet::getOutletIdentifier).toList();
    }

    public List<Integer> getOutletIdsByUser(String userId) {
        return entityManager.createNamedQuery("findOutletIdsByUser", Integer.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
