package org.example.community.comment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @AllArgsConstructor -> 모든 필드를 매개변수로 받는 생성자 자동으로 생성
 * DB 조회 결과를 Comment객체로 만들때 사용
 */
@Getter
@AllArgsConstructor
public class Comment {

    private Long id;
    private Long userId;
    private Long postId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}