package org.example.community.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 목록 조회용으로 필드의 일부분만 가져온다.
 */
@Getter
@AllArgsConstructor
public class PostSummaryResponse {

    @JsonProperty("post_id")
    private Long postId;

    private String title;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("like_count")
    private Long likeCount;

    @JsonProperty("comment_count")
    private Long commentCount;

    @JsonProperty("view_count")
    private Long viewCount;

    private PostWriterResponse writer;
}