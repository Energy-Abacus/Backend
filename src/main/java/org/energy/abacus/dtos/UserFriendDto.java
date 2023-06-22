package org.energy.abacus.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFriendDto {
    private String userId;
    private String username;
    private String picture;
    private boolean outgoing;
    private boolean accepted;
}