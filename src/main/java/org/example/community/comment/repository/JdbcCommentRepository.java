package org.example.community.comment.repository;

import lombok.RequiredArgsConstructor;
import org.example.community.comment.domain.Comment;
import org.example.community.comment.dto.response.CommentCreateResponse;
import org.example.community.comment.dto.response.CommentSummaryResponse;
import org.example.community.comment.dto.response.CommentUpdateResponse;
import org.example.community.comment.dto.response.CommentWriterResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * @RequiredArgsConstructor -> 초기화 되지않은 final 필드나, @NonNull 이 붙은 필드에 대해 생성자를 생성
 */
@Repository
@RequiredArgsConstructor
public class JdbcCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;


    @Override
    public Comment save(Comment comment) {
        String sql = """
                INSERT INTO comments (
                    user_id,
                    post_id,
                    content
                )
                VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setLong(1, comment.getUserId());
            ps.setLong(2, comment.getPostId());
            ps.setString(3, comment.getContent());

            return ps;
        }, keyHolder);

        Long commentId = keyHolder.getKey().longValue();

        return findById(commentId)
                .orElseThrow(() -> new IllegalStateException("댓글 저장 후 조회에 실패했습니다."));
    }

    /**
     * 작성자 정보도 들어가기 때문에 users 테이블과 JOIN
     */
    @Override
    public Optional<CommentCreateResponse> findCreateResponseById(Long commentId, Long loginUserId) {
        String sql = """
                SELECT
                    c.id AS comment_id,
                    c.content,
                    c.created_at,
                    CASE
                        WHEN c.user_id = ? THEN true
                        ELSE false
                    END AS is_writer,
                    u.id AS writer_id,
                    u.nickname AS writer_nickname,
                    u.profile_image AS writer_profile_image,
                    p.comment_count
                FROM comments c
                JOIN users u ON c.user_id = u.id
                JOIN posts p ON c.post_id = p.id
                WHERE c.id = ?
                """;

        List<CommentCreateResponse> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new CommentCreateResponse(
                        rs.getLong("comment_id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getBoolean("is_writer"),
                        new CommentWriterResponse(
                                rs.getLong("writer_id"),
                                rs.getString("writer_nickname"),
                                rs.getString("writer_profile_image")
                        ) ,rs.getLong("comment_count")
                ),
                loginUserId,
                commentId
        );

        /**
         * strem -> List 같은 여러 개의 데이터를 하나씩 흘려보내면서 처리할 수 있게 해주는 Java 기능
         * 조회 결과는 첫번째 댓글을 Optional로 반환
         * 없는것도 고려하여 안전하게 꺼내기 위함
         */
        return result.stream().findFirst();
    }

    /**
     *save내부에서만 쓰기때문에 private설정
     *
     */
    private Optional<Comment> findById(Long commentId) {
        String sql = """
                SELECT
                    id,
                    user_id,
                    post_id,
                    content,
                    created_at,
                    updated_at
                FROM comments
                WHERE id = ?
                """;

        List<Comment> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Comment(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getLong("post_id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                commentId
        );

        return result.stream().findFirst();
    }

    /**
     *게시글과 마찬가지로 커서의 유무를 파악하여 최초조회인지 추가조회인지 확인한다
     *게시글과 다른점은 특정 게시글의 댓글조회이므로 postId를 추가하였고
     * iswriter로 작성자인지 아닌지 구별하기위해 추가하였다.
     */
    @Override
    public List<CommentSummaryResponse> findCommentsByCursor(
            Long postId,
            Long cursor,
            Long loginUserId,
            int limit
    ) {
        if (cursor == null) {
            return findLatestComments(postId, loginUserId, limit);
        }

        return findNextComments(postId, cursor, loginUserId, limit);
    }

    private List<CommentSummaryResponse> findLatestComments(
            Long postId,
            Long loginUserId,
            int limit
    ) {
        String sql = """
            SELECT
                c.id AS comment_id,
                c.content AS content,
                c.created_at AS created_at,
                CASE
                    WHEN c.user_id = ? THEN true
                    ELSE false
                END AS is_writer,
                u.id AS user_id,
                u.nickname AS nickname,
                u.profile_image AS profile_image
            FROM comments c
            JOIN users u ON c.user_id = u.id
            WHERE c.post_id = ?
            ORDER BY c.id DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(
                sql,
                commentSummaryRowMapper(),
                loginUserId,
                postId,
                limit
        );
    }

    private List<CommentSummaryResponse> findNextComments(
            Long postId,
            Long cursor,
            Long loginUserId,
            int limit
    ) {
        String sql = """
            SELECT
                c.id AS comment_id,
                c.content AS content,
                c.created_at AS created_at,
                CASE
                    WHEN c.user_id = ? THEN true
                    ELSE false
                END AS is_writer,
                u.id AS user_id,
                u.nickname AS nickname,
                u.profile_image AS profile_image
            FROM comments c
            JOIN users u ON c.user_id = u.id
            WHERE c.post_id = ?
              AND c.id < ?
            ORDER BY c.id DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(
                sql,
                commentSummaryRowMapper(),
                loginUserId,
                postId,
                cursor,
                limit
        );
    }

    private RowMapper<CommentSummaryResponse> commentSummaryRowMapper() {
        return (rs, rowNum) -> {
            CommentWriterResponse writer = new CommentWriterResponse(
                    rs.getLong("user_id"),
                    rs.getString("nickname"),
                    rs.getString("profile_image")
            );

            return new CommentSummaryResponse(
                    rs.getLong("comment_id"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getBoolean("is_writer"),
                    writer
            );
        };
    }

    /**
     *이 메소드는 댓글 수정 전에 사용하는데
     * CommentId에 해당하는 댓글이 존재하는지, 그 댓글이 PostId에 속한 댓글인지
     * 댓글 작성자 userId를 가져와서 수정 권한을 확인
     */
    @Override
    public Optional<Long> findWriterIdByPostIdAndCommentId(Long postId, Long commentId) {
        String sql = """
            SELECT user_id
            FROM comments
            WHERE post_id = ? AND id = ?
            """;

        List<Long> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("user_id"),
                postId,
                commentId
        );

        return result.stream().findFirst();
    }

    /**
     *댓글 내용을 실제로 수정하는 메소드
     *단순히 수정만 하면됨
     * 체크는 앞에서 다 함
     */
    @Override
    public void updateComment(Long commentId, String content) {
        String sql = """
            UPDATE comments
            SET content = ?,
                updated_at = NOW()
            WHERE id = ?
            """;

        jdbcTemplate.update(sql, content, commentId);
    }

    /**
     *수정을 하고 응답을 조회하는 메소드
     */
    @Override
    public Optional<CommentUpdateResponse> findUpdateResponseById(Long commentId) {
        String sql = """
            SELECT id AS comment_id,
                   content,
                   updated_at
            FROM comments
            WHERE id = ?
            """;

        List<CommentUpdateResponse> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new CommentUpdateResponse(
                        rs.getLong("comment_id"),
                        rs.getString("content"),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                commentId
        );

        return result.stream().findFirst();
    }

    @Override
    public void deleteComment(Long commentId) {
        String sql = """
            DELETE FROM comments
            WHERE id = ?
            """;

        jdbcTemplate.update(sql, commentId);
    }


}