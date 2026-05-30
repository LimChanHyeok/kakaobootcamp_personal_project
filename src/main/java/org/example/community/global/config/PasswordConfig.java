package org.example.community.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 해싱에 사용할 PasswordEncoder를 Bean으로 등록하는 설정 클래스
 * 비밀번호 해싱 도구만 사용하기 위한 설정
 */
@Configuration
public class PasswordConfig {

    /**
     * BCrypt 기반 PasswordEncoder를 등록한다.
     *
     * 회원가입 시 비밀번호를 해싱하고,
     * 로그인 시 입력 비밀번호와 DB에 저장된 해시값을 비교할 때 사용한다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}