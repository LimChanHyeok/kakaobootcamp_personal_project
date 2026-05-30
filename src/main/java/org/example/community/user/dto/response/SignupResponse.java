package org.example.community.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SignupResponse {

    @JsonProperty("user_id")
    private final Long userId;

    public SignupResponse(Long userId) {
        this.userId = userId;
    }
}