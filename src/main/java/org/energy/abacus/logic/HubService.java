package org.energy.abacus.logic;

import lombok.extern.java.Log;
import org.energy.abacus.dtos.HubDto;
import org.energy.abacus.entities.Hub;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.NotAllowedException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@ApplicationScoped
@Log
public class HubService {

    @Inject
    EntityManager entityManager;

    public Hub addNewHub(final HubDto hubDto, String userId) {
        Hub hubEntity = Hub.builder()
                .name(hubDto.getName())
                .postToken(generateToken())
                .userid(userId)
                .build();
        entityManager.persist(hubEntity);
        return hubEntity;
    }

    public Hub getHubByToken(String postToken) {
        List<Hub> hubs = entityManager.createNamedQuery("findHubByToken", Hub.class)
                .setParameter("token", postToken)
                .getResultList();

        return hubs.isEmpty() ? null : hubs.get(0);
    }

    public Hub getHubById(int id, String userId) {
        Hub hub = entityManager.createNamedQuery("findHubById", Hub.class)
                .setParameter("id", id)
                .getSingleResult();

        if (!hub.getUserid().equals(userId)) {
            throw new NotAllowedException("Hub does not belong to user");
        }
        return hub;
    }

    public List<Hub> getAllHubsForUser(String userId) {
        return entityManager.createNamedQuery("findHubsByUserId", Hub.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    private String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return Base64.getEncoder().encodeToString(token);
    }
}
