package org.energy.abacus.logic;

import lombok.extern.java.Log;
import org.energy.abacus.dtos.OutletDto;
import org.energy.abacus.entities.Hub;
import org.energy.abacus.entities.Outlet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Collection;

@ApplicationScoped
@Log
public class OutletService {

    @Inject
    EntityManager entityManager;

    @Inject
    HubService hubService;

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
        /*return entityManager.createNamedQuery("findOutletsByHubId", Outlet.class)
                .setParameter("hubId", hubId)
                .getResultList();*/
    }
}
