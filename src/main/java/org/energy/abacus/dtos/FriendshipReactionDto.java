package org.energy.abacus.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FriendshipReactionDto {
    private String receiver;
    private boolean accept;
}
