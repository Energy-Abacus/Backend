package org.energy.abacus.dtos;

import lombok.Getter;

@Getter
public class FriendshipDto {
    private String requestReceiverId;
    private String requestSenderId;
    private boolean accepted;
}
