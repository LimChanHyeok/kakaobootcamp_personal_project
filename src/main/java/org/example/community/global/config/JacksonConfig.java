package org.example.community.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JacksonConfig {

    /**
     *Java 객체와 JSON 문자열을 서로 변환해주는 도구
     * cursor를 만들고 해석할 때 사용
     * 마지막 id를 JSON문자열로 바꿔줌
     * 자동으로 라이브러리를 인식을 못해서 직접 작성하고 빈으로 등록
     *
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}