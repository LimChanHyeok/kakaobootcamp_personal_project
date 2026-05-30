package org.example.community.global.exception;

import lombok.Getter;

/**
 * Service 계층에서 회원 중복, 비밀번호 불일치 같은
 * 비즈니스 오류가 발생했을 때 이 예외를 던진다.
 */
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}