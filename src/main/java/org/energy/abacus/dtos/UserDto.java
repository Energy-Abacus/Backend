package org.energy.abacus.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    private String user_id;
    private String username;
    private String picture;
}
