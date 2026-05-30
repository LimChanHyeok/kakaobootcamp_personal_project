package org.example.community.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 댓글 목록 조회에서 댓글 하나의 정보를 담는 응답 DTO
 */
@Getter
@AllArgsConstructor
public class CommentSummaryResponse {

    @JsonProperty("comment_id")
    private Long commentId;

    private String content;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("is_writer")
    private boolean writerStatus;

    private CommentWriterResponse writer;
}