package org.example.community.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 댓글 작성자 응답 DTO
 */
@Getter
@AllArgsConstructor
public class CommentWriterResponse {

    @JsonProperty("user_id")
    private Long userId;

    private String nickname;

    @JsonProperty("profile_image")
    private String profileImage;
}