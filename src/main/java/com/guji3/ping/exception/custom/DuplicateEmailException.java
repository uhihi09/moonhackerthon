package com.guji3.ping.exception.custom;

/**
 * 이메일 중복 시 발생하는 예외
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super(String.format("이미 사용 중인 이메일입니다: %s", email));
    }
}