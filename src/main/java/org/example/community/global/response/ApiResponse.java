package org.example.community.global.response;

import lombok.Getter;

/**
 * 모든 API 응답 형식을 통일하기 위한 공통 응답 클래스이다.
 *
 * 성공 응답과 실패 응답 모두 같은 구조로 내려준다.
 */
@Getter
public class ApiResponse<T> {

    /**
     * 실패 응답에서 프론트엔드가 에러를 구분하기 위한 코드이다.
     * 성공 응답에선 null이 들어간다.
     */
    private final String code;

    /**
     * 클라이언트에게 전달할 응답 메시지이다.
     */
    private final String message;

    /**
     * 성공 응답에서 실제 데이터를 담는 필드이다.
     *
     * 실패 응답에서는 null이다.
     */
    private final T data;

    /**
     * 생성자를 private로 함으로써 개발자가 임의로 응답 방식을 못바꾸게함
     */
    private ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 성공 응답을 생성한다.
     *<T> 제네릭을 둠으로써 각각의 상황에 맞는 타입을 넣을 수 있도록 하였다.
     * 예를 들어 회원가입이면 SignupResponse 타입
     * 성공이기 때문에 상태코드는 null
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(null, message, data);
    }

    /**
     * 실패 응답을 생성한다.
     * 실패이기 때문에 data는 null이다
     * <Void>를 해주는 이유는 데이터에 값이 없다는 것을 표현해주기 위해서이다.
     */
    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}