package org.example.community.postlike.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostLike {

    private Long userId;
    private Long postId;
    private LocalDateTime createdAt;
}