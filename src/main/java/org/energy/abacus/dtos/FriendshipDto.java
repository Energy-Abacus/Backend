package org.energy.abacus.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendshipDto {
    private String requestReceiverId;
    private String requestSenderId;
    private boolean accepted;
}
