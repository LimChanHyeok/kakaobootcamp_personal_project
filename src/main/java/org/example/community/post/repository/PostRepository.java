package org.example.community.post.repository;

import org.example.community.post.domain.Post;
import org.example.community.post.dto.response.PostCreateResponse;
import org.example.community.post.dto.response.PostDetailResponse;
import org.example.community.post.dto.response.PostSummaryResponse;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    List<PostSummaryResponse> findPostsByCursor(Long cursor, int limit);

    Optional<Post> findById(Long postId);

    PostCreateResponse save(Post post);

    Optional<PostDetailResponse> findPostDetailById(Long postId, Long loginUserId);

    /**
     *게시글을 조회하고, 게시글을 수정하는 메소드
     * 조회를 하고 게시글이 있는 지 파악하고 나서 수정을 해야하기 때문
     * 또한 수정용으로 필요한 값만 조회하도록 하였음
     */
    Optional<Long> findWriterIdByPostId(Long postId);

    void updatePost(Long postId, String title, String content, String imageUrl);

    void deleteById(Long postId);

    /**
     * 댓글 개수 증가시키는 메소드
     */
    void increaseCommentCount(Long postId);

    /**
     * 댓글 개수 감소시키는 메소드
     */
    void decreaseCommentCount(Long postId);

    /**
     *조회수 증가시키는 메소드
     */
    void increaseViewCount(Long postId);

    /**
     * 좋아요 수 증가시키는 메소드
     */
    void increaseLikeCount(Long postId);

    /**
     *게시글에 좋아요수를 찾기 위한 메소드
     */
    int findLikeCountByPostId(Long postId);

    /**
     *좋아요 수 -1시키는 메소드
     */
    void decreaseLikeCount(Long postId);
}