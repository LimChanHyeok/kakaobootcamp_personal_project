package org.example.community.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 댓글 목록 조회 응답 DTO
 */
@Getter
@AllArgsConstructor
public class CommentListResponse {

    private List<CommentSummaryResponse> comments;

    private String cursor;

    @JsonProperty("has_next")
    private boolean hasNext;
}