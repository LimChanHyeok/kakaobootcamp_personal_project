package org.example.community.postlike.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcPostLikeRepository implements PostLikeRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * post_like 테이블에서 특정 사용자가 특정 게시물에 좋아요를 눌렀는지 개수를 세는 메소드
     * 만약 1이면 누른거고 0이거나 null이면 누르지 않은것
     */
    @Override
    public boolean existsByPostIdAndUserId(Long postId, Long userId) {
        String sql = """
                SELECT COUNT(*)
                FROM post_like
                WHERE post_id = ? AND user_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                postId,
                userId
        );

        return count != null && count > 0;
    }

    /**
     * 테이블에 저장하는 메소드
     */
    @Override
    public void save(Long postId, Long userId) {
        String sql = """
                INSERT INTO post_like (user_id, post_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.update(sql, userId, postId);
    }

    /**
     * post_like 테이블에 해당 유저가 해당 게시글에 누른 좋아요 기록을 지움
     */
    @Override
    public void delete(Long postId, Long userId) {
        String sql = """
            DELETE FROM post_like
            WHERE post_id = ? AND user_id = ?
            """;

        jdbcTemplate.update(sql, postId, userId);
    }
}