package org.example.community.postlike.repository;

public interface PostLikeRepository {

    /**
     * 현재 사용자가 해당 게시글에 이미 좋아요를 눌렀는지 확인
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * Post_like 테이블에 user_id와 post_id 저장
     */
    void save(Long postId, Long userId);

    void delete(Long postId, Long userId);
}