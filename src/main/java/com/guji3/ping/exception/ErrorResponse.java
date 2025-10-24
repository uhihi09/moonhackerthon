package com.guji3.ping.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 통일된 에러 응답 형식
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값은 JSON에 포함 안 함
public class ErrorResponse {

    // 기본 정보
    private Boolean success;              // 항상 false
    private LocalDateTime timestamp;      // 오류 발생 시각
    private Integer status;               // HTTP 상태 코드 (400, 500 등)
    private String error;                 // 오류 타입 (Bad Request, Internal Server Error)
    private String message;               // 사용자에게 보여줄 메시지
    private String path;                  // 오류 발생 API 경로

    // 상세 정보 (선택)
    private String exception;             // 예외 클래스명 (개발 환경에서만)
    private String trace;                 // 스택 트레이스 (개발 환경에서만)
    private Map<String, String> details;  // Validation 오류 상세 정보

    /**
     * 간단한 에러 응답 생성 (해커톤용)
     */
    public static ErrorResponse of(int status, String error, String message) {
        return ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .build();
    }

    /**
     * Validation 에러 응답 생성
     */
    public static ErrorResponse of(int status, String error, String message, Map<String, String> details) {
        return ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .details(details)
                .build();
    }
}