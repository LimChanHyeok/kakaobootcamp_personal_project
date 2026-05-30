package org.example.community.comment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.community.comment.domain.Comment;
import org.example.community.comment.dto.response.CommentCreateResponse;
import org.example.community.comment.dto.response.CommentListResponse;
import org.example.community.comment.dto.response.CommentSummaryResponse;
import org.example.community.comment.dto.response.CommentUpdateResponse;
import org.example.community.comment.repository.CommentRepository;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.example.community.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    /**
     * ObjectMapper는 java 객체와 JSON 문자열을 변환하는 도구
     * import com.fasterxml.jackson.databind.ObjectMapper;
     * 이 부분이 읽어지지 않아 gradle에 직접 추가
     * 또한 Bean등록이 안되어서 config.JacksonConfig에 @Bean으로 ObjectMapper등록
     *
     */
    private final ObjectMapper objectMapper;

    @Transactional
    public CommentCreateResponse createComment(
            Long postId,
            Long loginUserId,
            String content
    ) {
        postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Comment comment = new Comment(
                null,
                loginUserId,
                postId,
                content,
                null,
                null
        );

        Comment savedComment = commentRepository.save(comment);

        /**
         * 이 부분에서 게시그르이 댓글 수를 증가시킨다.
         */
        postRepository.increaseCommentCount(postId);

        return commentRepository.findCreateResponseById(
                        savedComment.getId(),
                        loginUserId
                )
                .orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    @Transactional(readOnly = true)
    public CommentListResponse getComments(
            Long postId,
            Long loginUserId,
            String cursor,
            Integer size
    ) {
        postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        /**
         * 밑에 작성한 함수로서 null이면 DEFAULT_SIZE 반환
         * 1보다 작거나 MAX_SIZE보다 크면 BAD_REQUEST 예외 던짐
         */
        int validatedSize = validateSize(size);

        /**
         * 클라이언트가 보낸 cursor 문자열을 실제 댓글 id로 바꾸는 부분
         */
        Long decodedCursor = decodeCursor(cursor);

        /**
         * 여기서 DB에 댓글조회 요청
         * 여기서 validatedSize에 +1을 하면서 다음페이지가 있는지 확인함
         * 10개를 요청했지만 11개를 조회하면서 뒤에 더있다는것을 알림
         */
        List<CommentSummaryResponse> fetchedComments =
                commentRepository.findCommentsByCursor(
                        postId,
                        decodedCursor,
                        loginUserId,
                        validatedSize + 1
                );

        boolean hasNext = fetchedComments.size() > validatedSize;

        List<CommentSummaryResponse> comments = fetchedComments;

        if (hasNext) {
            comments = fetchedComments.subList(0, validatedSize);
        }

        /**
         * 다음 페이지를 조회할 때 사용할 cursor 만들기
         * 응답으로 내려준 댓글 목록의 마지막 댓글 id를 기준으로 cursor 만듬
         * createNextCursor함수를 이용하여 인코딩된 nextCursor를 만드는 것
         */
        String nextCursor = createNextCursor(comments, hasNext);

        return new CommentListResponse(
                comments,
                nextCursor,
                hasNext
        );
    }

    private int validateSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        if (size < 1 || size > MAX_SIZE) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        return size;
    }

    private Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            String json = new String(decodedBytes, StandardCharsets.UTF_8);

            Map<String, Object> cursorMap = objectMapper.readValue(json, Map.class);

            Object commentId = cursorMap.get("commentId");

            if (commentId == null) {
                throw new CustomException(ErrorCode.BAD_REQUEST);
            }

            return Long.valueOf(commentId.toString());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
    }

    private String createNextCursor(
            List<CommentSummaryResponse> comments,
            boolean hasNext
    ) {
        if (!hasNext || comments.isEmpty()) {
            return null;
        }

        try {
            CommentSummaryResponse lastComment = comments.get(comments.size() - 1);

            Map<String, Long> cursorMap = Map.of(
                    "commentId", lastComment.getCommentId()
            );

            String json = objectMapper.writeValueAsString(cursorMap);

            return Base64.getEncoder()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }@Transactional
    public CommentUpdateResponse updateComment(
            Long postId,
            Long commentId,
            Long loginUserId,
            String content
    ) {
        postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Long writerId = commentRepository.findWriterIdByPostIdAndCommentId(postId, commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!writerId.equals(loginUserId)) {
            throw new CustomException(ErrorCode.COMMENT_UPDATE_FORBIDDEN);
        }

        commentRepository.updateComment(commentId, content);

        return commentRepository.findUpdateResponseById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }

    @Transactional
    public void deleteComment(
            Long postId,
            Long commentId,
            Long loginUserId
    ) {
        postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Long writerId = commentRepository.findWriterIdByPostIdAndCommentId(postId, commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!writerId.equals(loginUserId)) {
            throw new CustomException(ErrorCode.COMMENT_DELETE_FORBIDDEN);
        }

        commentRepository.deleteComment(commentId);

        postRepository.decreaseCommentCount(postId);
    }


}