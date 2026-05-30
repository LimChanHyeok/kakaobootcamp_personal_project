package org.example.community.post.repository;

import lombok.RequiredArgsConstructor;
import org.example.community.post.domain.Post;
import org.example.community.post.dto.response.PostCreateResponse;
import org.example.community.post.dto.response.PostDetailResponse;
import org.example.community.post.dto.response.PostSummaryResponse;
import org.example.community.post.dto.response.PostWriterResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcPostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     *게시글 목록 조회를 어떻게 할지 판단하는 메소드
     * cursor가 null이라면 최초 조회이기 때문에 findLatesPosts(limit) 호출
     * cursor가 존재한다면 추가 조회이기 때문에 findNextPosts(cusor,limit)호출
     * 여기서 limit은 가져올 게시글 개수!
     */
    @Override
    public List<PostSummaryResponse> findPostsByCursor(Long cursor, int limit) {
        if (cursor == null) {
            return findLatestPosts(limit);
        }

        return findNextPosts(cursor, limit);
    }

    @Override
    public Optional<Post> findById(Long postId) {
        String sql = """
            SELECT
                id,
                user_id,
                title,
                content,
                image_url,
                view_count,
                like_count,
                comment_count,
                created_at,
                updated_at
            FROM posts
            WHERE id = ?
            """;

        List<Post> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("image_url"),
                        rs.getLong("view_count"),
                        rs.getLong("like_count"),
                        rs.getLong("comment_count"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                postId
        );

        return result.stream().findFirst();
    }

    /**
     * users 테이블과 JOIN하여 게시글 목록과 작성자 정보 가져옴
     * ?에는 limit값이 들어감
     */
    private List<PostSummaryResponse> findLatestPosts(int limit) {
        String sql = """
                SELECT
                    p.id AS post_id,
                    p.title AS title,
                    p.created_at AS created_at,
                    p.like_count AS like_count,
                    p.comment_count AS comment_count,
                    p.view_count AS view_count,
                    u.id AS user_id,
                    u.nickname AS nickname,
                    u.profile_image AS profile_image
                FROM posts p
                JOIN users u ON p.user_id = u.id
                ORDER BY p.id DESC
                LIMIT ?
                """;
        /**
         * jdbcTemplate.qeury(실행할 sql문, 결과를 객체로 바꿔주는 RowMapper,SQL의 ?에 들어갈 값)
         */
        return jdbcTemplate.query(sql, postSummaryRowMapper(), limit);
    }

    private List<PostSummaryResponse> findNextPosts(Long cursor, int limit) {
        String sql = """
                SELECT
                    p.id AS post_id,
                    p.title AS title,
                    p.created_at AS created_at,
                    p.like_count AS like_count,
                    p.comment_count AS comment_count,
                    p.view_count AS view_count,
                    u.id AS user_id,
                    u.nickname AS nickname,
                    u.profile_image AS profile_image
                FROM posts p
                JOIN users u ON p.user_id = u.id
                WHERE p.id < ?
                ORDER BY p.id DESC
                LIMIT ?
                """;
        /**
         * jdbcTemplate.qeury(실행할 sql문, 결과를 객체로 바꿔주는 RowMapper,SQL의 ?에 들어갈 값)
         * 여기서 cursor는 조회한 마지막 페이지의 id가 들어가고 만약 마지막 id가 11이라면 p.id<11이므로 10이하로 limit만큼 조회한다.
         */
        return jdbcTemplate.query(sql, postSummaryRowMapper(), cursor, limit);
    }

    /**
     * 여기도 마찬가지로 조회 결과가 객체로 나오는 것이 아니기 때문에
     * ResultSet으로 받아지는 DB 조회결과 한줄을 java 객체 하나로 바궈준다.
     *
     */
    private RowMapper<PostSummaryResponse> postSummaryRowMapper() {
        return (rs, rowNum) -> {
            /**
             * 여기서 DTO를 만들고있음
             * PostListResponse가 아닌 PostSummaryResponse를 리턴 받는 이유는
             * DB에 저장된 것 외에도 hasNext,nextCurosr등을 포함시켜야하기 때문이다.
             */
            PostWriterResponse writer = new PostWriterResponse(
                    rs.getLong("user_id"),
                    rs.getString("nickname"),
                    rs.getString("profile_image")
            );

            return new PostSummaryResponse(
                    rs.getLong("post_id"),
                    rs.getString("title"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getLong("like_count"),
                    rs.getLong("comment_count"),
                    rs.getLong("view_count"),
                    writer
            );
        };
    }

    @Override
    public PostCreateResponse save(Post post) {
        String sql = """
            INSERT INTO posts (
                user_id,
                title,
                content,
                image_url
            )
            VALUES (?, ?, ?, ?)
            """;

        /**
         * keyHolder로 저장한 id 받아오기
         */
        KeyHolder keyHolder = new GeneratedKeyHolder();

        /**
         * update는 insert,update,delete같은 db 변경 작업을 실행할 때 사용함
         * Statement.RETURN_GENERATED_KEYS는 db가 자동 생성한 id를 돌려달라는 것
         */
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setLong(1, post.getUserId());
            ps.setString(2, post.getTitle());
            ps.setString(3, post.getContent());
            ps.setString(4, post.getImageUrl());

            return ps;
        }, keyHolder);

        Long postId = keyHolder.getKey().longValue();

        /**
         * 게시글을 저장한다음 방금 생선된 postId를 통해 다시 받아옴
         * 응답에 필요한 데이터들을 가져오기 위해!
         */
        return findCreatedPostById(postId);
    }

    private PostCreateResponse findCreatedPostById(Long postId) {
        String sql = """
            SELECT
                p.id AS post_id,
                p.title AS title,
                p.content AS content,
                p.image_url AS image_url,
                p.created_at AS created_at,
                u.id AS user_id,
                u.nickname AS nickname,
                u.profile_image AS profile_image
            FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE p.id = ?
            """;
        /**
         * queryForObject는 SELECT결과가 한개일때 사용하는 메소드
         * 결과 한 줄을 RowMapper로 변환하는 것
         */
        return jdbcTemplate.queryForObject(
                sql,
                postCreateRowMapper(),
                postId
        );
    }

    /**
     * 이것 또한 RowMapper로 DB결과조회한 것을 PostCreateResponse로 바꾸기 위해
     */
    private RowMapper<PostCreateResponse> postCreateRowMapper() {
        return (rs, rowNum) -> {
            PostWriterResponse writer = new PostWriterResponse(
                    rs.getLong("user_id"),
                    rs.getString("nickname"),
                    rs.getString("profile_image")
            );

            return new PostCreateResponse(
                    rs.getLong("post_id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("image_url"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    writer
            );
        };
    }

    /**
     *여기서 Login한 userId를 받는 이유는 현재 로그인한 유저와 게시글의 작성자를 비교해서
     * 두개가 일치한다면 수정/삭제 버튼을 보여주도록 하기 위해서
     */
    @Override
    public Optional<PostDetailResponse> findPostDetailById(Long postId, Long loginUserId) {
        /**
         * 게시글 상세 조회 SQL
         * posts 테이블에서 게시글 정보를 조회하고
         * users 테이블과 JOIN해서 작성자 정보를 함께 조회
         *
         * loginUserId는 현재 로그인한 사용자 id이다.
         * 이 값은 게시글 작성자와 현재 로그인 사용자가 같은지 비교해서
         * is_writer 값을 만들기 위해 사용
         *
         * LEFT JOIN을 쓰는 이유는 좋아요를 안눌렀어도 게시글은 조회되어야하기 때문
         * 좋아요를 안눌렀어도 게시글 상세조회는 해야됨
         */
        String sql = """
        SELECT
            p.id AS post_id,
            p.title,
            p.content,
            p.image_url,
            p.created_at,
            p.like_count,
            p.comment_count,
            p.view_count,
            CASE
                WHEN p.user_id = ? THEN true
                ELSE false
            END AS is_writer,
            CASE
                WHEN pl.user_id IS NOT NULL THEN true
                ELSE false
            END AS is_liked,
            u.id AS writer_id,
            u.nickname AS writer_nickname,
            u.profile_image AS writer_profile_image
        FROM posts p
        JOIN users u ON p.user_id = u.id
        LEFT JOIN post_like pl
            ON pl.post_id = p.id
           AND pl.user_id = ?
        WHERE p.id = ?
        """;

        /**
         * jdbcTemplate.query()는 SQL 실행 결과를 여러 행의 List로 반환
         *
         * 게시글 id는 기본키이기 때문에 실제 결과는 0개 또는 1개지만
         * query()는 기본적으로 List 형태로 결과를 반환
         */
        List<PostDetailResponse> result = jdbcTemplate.query(sql,

                /**
                 * ResultSet(rs)에 담긴 DB 조회 결과 한 행을
                 * PostDetailResponse 객체로 변환
                 */
                (rs, rowNum) -> new PostDetailResponse(
                        rs.getLong("post_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("image_url"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getInt("like_count"),
                        rs.getInt("comment_count"),
                        rs.getInt("view_count"),
                        rs.getBoolean("is_liked"),
                        rs.getBoolean("is_writer"),

                        /**
                         * writer 필드는 객체 형태로 응답해야 하므로
                         * PostWriterResponse를 따로 생성해서 넣는다
                         */
                        new PostWriterResponse(
                                rs.getLong("writer_id"),
                                rs.getString("writer_nickname"),
                                rs.getString("writer_profile_image")
                        )
                ), loginUserId,loginUserId, postId
        );

        /**
         * 조회 결과가 있으면 첫 번째 게시글을 Optional에 담아 반환하고,
         * 결과가 없으면 Optional.empty()를 반환
         */
        return result.stream().findFirst();
    }

    /**
     *수정하거나 삭제할 때 게시글의 작성자와 로그인한 사용자가 같은지 아닌지 판단하기 위한 메소드
     */
    @Override
    public Optional<Long> findWriterIdByPostId(Long postId) {
        String sql = """
            SELECT user_id
            FROM posts
            WHERE id = ?
            """;

        List<Long> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("user_id"),
                postId
        );

        return result.stream().findFirst();
    }

    @Override
    public void updatePost(Long postId, String title, String content, String imageUrl) {
        String sql = """
            UPDATE posts
            SET title = ?,
                content = ?,
                image_url = COALESCE(?, image_url),
                updated_at = NOW()
            WHERE id = ?
            """;

        jdbcTemplate.update(
                sql,
                title,
                content,
                imageUrl,
                postId
        );
    }

    @Override
    public void deleteById(Long postId) {
        String sql = """
            DELETE FROM posts
            WHERE id = ?
            """;

        jdbcTemplate.update(sql, postId);
    }

    @Override
    public void increaseCommentCount(Long postId) {
        String sql = """
            UPDATE posts
            SET comment_count = comment_count + 1
            WHERE id = ?
            """;

        jdbcTemplate.update(sql, postId);
    }

    @Override
    public void decreaseCommentCount(Long postId) {
        String sql = """
            UPDATE posts
            SET comment_count = comment_count - 1
            WHERE id = ? AND comment_count > 0
            """;

        jdbcTemplate.update(sql, postId);
    }

    /**
     *조회수 증가시키는 메소드 구드
     */
    @Override
    public void increaseViewCount(Long postId) {
        String sql = """
            UPDATE posts
            SET view_count = view_count + 1
            WHERE id = ?
            """;

        jdbcTemplate.update(sql, postId);
    }

    /**
     *이 메소드는 게시글의 좋아요 수를 1증가시키는 메소드
     * post_like테이블에 저장하고 post테이블에 like_count에 +1함
     */
    @Override
    public void increaseLikeCount(Long postId) {
        String sql = """
            UPDATE posts
            SET like_count = like_count + 1
            WHERE id = ?
            """;

        jdbcTemplate.update(sql, postId);
    }

    /**
     * 좋아요 수를 조회하는 메소드
     * 좋아요 등록 응답에 좋아요 수를 응답해줘야하기 때문
     * 서버가 DB에서 조회해서 내려주기 때문에 프론트는 바로 적용하면됨
     */
    @Override
    public int findLikeCountByPostId(Long postId) {
        String sql = """
            SELECT like_count
            FROM posts
            WHERE id = ?
            """;

        Integer likeCount = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                postId
        );

        return likeCount != null ? likeCount : 0;
    }


    @Override
    public void decreaseLikeCount(Long postId) {
        String sql = """
            UPDATE posts
            SET like_count = like_count - 1
            WHERE id = ? AND like_count > 0
            """;

        jdbcTemplate.update(sql, postId);
    }




}