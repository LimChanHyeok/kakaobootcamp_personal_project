package org.example.community.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.community.comment.dto.request.CommentCreateRequest;
import org.example.community.comment.dto.request.CommentUpdateRequest;
import org.example.community.comment.dto.response.CommentCreateResponse;
import org.example.community.comment.dto.response.CommentListResponse;
import org.example.community.comment.dto.response.CommentUpdateResponse;
import org.example.community.comment.service.CommentService;
import org.example.community.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentCreateResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        /**
         * 이 부분도 JWT구현후 변경
         */
        Long loginUserId = 1L;

        CommentCreateResponse response = commentService.createComment(
                postId,
                loginUserId,
                request.getContent()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("댓글 등록에 성공했습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CommentListResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    ) {
        Long loginUserId = 1L;

        CommentListResponse response = commentService.getComments(
                postId,
                loginUserId,
                cursor,
                size
        );

        return ResponseEntity.ok(
                ApiResponse.success("댓글 목록 조회에 성공했습니다.", response)
        );
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentUpdateResponse>> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        Long loginUserId = 1L;

        CommentUpdateResponse response = commentService.updateComment(
                postId,
                commentId,
                loginUserId,
                request.getContent()
        );

        return ResponseEntity.ok(
                ApiResponse.success("댓글 수정에 성공했습니다.", response)
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Long loginUserId = 1L;

        commentService.deleteComment(postId, commentId, loginUserId);

        return ResponseEntity.ok(
                ApiResponse.success("댓글 삭제에 성공했습니다.", null)
        );
    }
}