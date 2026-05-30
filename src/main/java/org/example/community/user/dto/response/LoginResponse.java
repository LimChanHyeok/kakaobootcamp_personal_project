package org.example.community.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인 성공 시 클라이언트에게 반환하는 응답 DTO
 */
@Getter
@AllArgsConstructor
public class LoginResponse {

    /**
     * 로그인에 성공했을 때 유저 id반환
     * 나중에 JWT를 쓸때는 어떻게 할까가 고민임 필요가 없을까..?
     */
    @JsonProperty("user_id")
    private Long userId;
}