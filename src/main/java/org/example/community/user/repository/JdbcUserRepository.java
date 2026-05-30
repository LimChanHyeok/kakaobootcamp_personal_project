package org.example.community.user.repository;

import lombok.RequiredArgsConstructor;
import org.example.community.user.domain.User;
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
 * UserRepository 인터페이스를 JDBC 방식으로 구현한 클래스
 * 나중에 JPA로 쉽게 바꾸기 위해 UserRepository 사용
 */
@Repository
@RequiredArgsConstructor
public class JdbcUserRepository implements UserRepository {
    /**
     * JdbcTemplate은 JDBC 사용 시 반복적으로 작성해야 하는
     * Connection 생성, PreparedStatement 실행, ResultSet 처리 등의 작업을 줄여준다.
     */
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User save(User user) {
        /**
         * 회원 정보를 users 테이블에 저장한다.
         *
         * id, created_at, updated_at은 DB에서 자동 생성되기 때문에
         * INSERT 문에는 email, password, nickname, profile_image만 넣는다.
         */
        String sql = """
                INSERT INTO users (email, password, nickname, profile_image)
                VALUES (?, ?, ?, ?)
                """;
        /**
         * INSERT 이후에 DB가 자동 생성한 id를 담아두는 객체다
         * 이걸 담아두어야 나중에 응답할 때 유저id반환이 가능하다
         */
        KeyHolder keyHolder = new GeneratedKeyHolder();

        /**
         * 여기서 Statement.RETURN_GENERATED_KEY를 이용하여 아까 키 홀더에 담아두었던 id를 반환하도록 요청하는것
         */
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getNickname());
            ps.setString(4, user.getProfileImage());

            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        /**
         * 여기서 findById의 반환 타입은 Optional<User>이다 Optional안에 값이 있으면 값을 꺼내고 없으면 예외를 던짐
         */
        return findById(id)
                .orElseThrow(() -> new IllegalStateException("회원 저장 후 조회에 실패했습니다."));
    }

    /**
     * id를 기준으로 회원 조회하기
     * 조회 결과가 없을 수 있기 때문에 Optional<User>를 반환
     */
    @Override
    public Optional<User> findById(Long id) {
        String sql = """
                SELECT id, email, password, nickname, profile_image, created_at, updated_at
                FROM users
                WHERE id = ?
                """;
        /**
         * 여기서 querysms SELECT 결과를 조회할 때 쓰는 메소드
         * jdbcTemplate query는 기본적으로 list로 반환하는데 id는 어차피 중복이 안되기 때문에 0개 또는 1개이다.
         * jdbcTemplate.query(실행할 sql,결과를 객체로 바꾸는 방법, SQL에 넣을값)
         */
        List<User> users = jdbcTemplate.query(sql, userRowMapper(), id);
        /**
         * 반환 타입이 Optional이기 때문에 안전하게 처리하기 위해 stream.findFirst를 사용
         */
        return users.stream().findFirst();
    }

    /**
     * 이메일 기준으로 회원 조회하기
     */
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = """
                SELECT id, email, password, nickname, profile_image, created_at, updated_at
                FROM users
                WHERE email = ?
                """;

        List<User> users = jdbcTemplate.query(sql, userRowMapper(), email);

        return users.stream().findFirst();
    }

    /**
     * 이메일 중복 여부 확인하기
     */
    @Override
    public boolean existsByEmail(String email) {
        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE email = ?
                """;

        /**
         * queryForObject는 단일 값 조회할 때 사용
         * 어차피 하나만 있어도 중복이기 때문
         */
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);

        return count != null && count > 0;
    }

    /**
     * 닉네임 중복 여부 확인
     */
    @Override
    public boolean existsByNickname(String nickname) {
        String sql = """
            SELECT COUNT(*)
            FROM users
            WHERE nickname = ?
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nickname);

        return count != null && count > 0;
    }

    @Override
    public void updateProfile(Long userId, String nickname, String profileImage) {
        /**
         * COALESCE -> 인자를 첫번째자리부터 비교하며 NULL을 만나면 다음으로 미루고, NULL값이 아닌값을 만나면 그 값을 뽑아냄
         * PATCH를 이용하기 때문에 NULL값이 올 수 있기 때문에 사용
         */
        String sql = """
            UPDATE users
            SET nickname = COALESCE(?, nickname),
                profile_image = COALESCE(?, profile_image),
                updated_at = NOW()
            WHERE id = ?
            """;

        jdbcTemplate.update(sql, nickname, profileImage, userId);
    }

    /**
     * 평문 비밀번호대신 encodePassword로 파라미터 변수 지정
     */
    @Override
    public void updatePassword(Long userId, String encodedPassword) {
        String sql = """
            UPDATE users
            SET password = ?,
                updated_at = NOW()
            WHERE id = ?
            """;

        jdbcTemplate.update(sql, encodedPassword, userId);
    }

    @Override
    public void deleteById(Long userId) {
        String sql = """
            DELETE FROM users
            WHERE id = ?
            """;

        jdbcTemplate.update(sql, userId);
    }

    /**
     * 아주 중요한 부분!!
     * JDBC에서 DB 조회 결과를 자바 객체로 바꾸는 코드
     * 먼저 DB 조회 결과는 ResultSet이라는 곳에 담기게 되는데 데이터베이스 형식으로 저장되기 때문에 객체형식으로 바꿔줘야한다.
     * RowMapper<User>는 DB 결과 한줄을 User객체 하나로 바꿔주는 도구이다.
     * 밑에 보이는 rs가 ResultSet이다
     * 또한 DB는 스네이크케이스, 자바 필드는 카멜 케이스이므로 이 메소드에서 수동으로 매핑
     */
    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("nickname"),
                rs.getString("profile_image"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }


}