package org.example.community.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 겟글 목록 조회 응답의 전체 writer를 postSummary가 감싸고
 * postSummary를 PostList가 감싸서 최종 응답을 준다
 */
@Getter
@AllArgsConstructor
public class PostListResponse {

    private List<PostSummaryResponse> posts;

    @JsonProperty("next_cursor")
    private String nextCursor;

    @JsonProperty("has_next")
    private boolean hasNext;
}