package com.guji3.ping.exception.custom;

/**
 * 긴급 신호 처리 중 오류 발생 시 예외
 */
public class EmergencyProcessException extends RuntimeException {

    public EmergencyProcessException(String message) {
        super(message);
    }

    public EmergencyProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}