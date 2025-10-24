package com.guji3.ping.exception.custom;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String email, String field) {
        super(String.format("사용자를 찾을 수 없습니다: %s = %s", field, email));
    }

    public UserNotFoundException(Long userId) {
        super(String.format("사용자를 찾을 수 없습니다: ID = %d", userId));
    }
}