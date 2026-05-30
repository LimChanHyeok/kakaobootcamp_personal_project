package org.example.community.comment.repository;

import org.example.community.comment.domain.Comment;
import org.example.community.comment.dto.response.CommentCreateResponse;
import org.example.community.comment.dto.response.CommentSummaryResponse;
import org.example.community.comment.dto.response.CommentUpdateResponse;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Comment save(Comment comment);

    Optional<CommentCreateResponse> findCreateResponseById(Long commentId, Long loginUserId);

    List<CommentSummaryResponse> findCommentsByCursor(
            Long postId,
            Long cursor,
            Long loginUserId,
            int limit
    );

    /**
     *댓글을 수정하기 위해 댓글 존재여부와 작성자를 확인하고
     * 댓글 내용을 수정하고
     * 댓글 응답을 조회하기 위한 메소드
     */
    Optional<Long> findWriterIdByPostIdAndCommentId(Long postId, Long commentId);

    void updateComment(Long commentId, String content);

    Optional<CommentUpdateResponse> findUpdateResponseById(Long commentId);

    void deleteComment(Long commentId);
}