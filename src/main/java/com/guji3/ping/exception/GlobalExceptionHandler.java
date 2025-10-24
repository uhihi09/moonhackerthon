package com.guji3.ping.exception;

import com.guji3.ping.exception.custom.DeviceNotRegisteredException;
import com.guji3.ping.exception.custom.DuplicateEmailException;
import com.guji3.ping.exception.custom.EmergencyProcessException;
import com.guji3.ping.exception.custom.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ===================================
    // 1. 일반 예외 (최종 안전망)
    // ===================================

    /**
     * 처리되지 않은 모든 예외를 여기서 잡습니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("❌ [서버 오류] {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                .path(request.getRequestURI())
                .exception(e.getClass().getSimpleName())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ===================================
    // 2. 비즈니스 로직 예외
    // ===================================

    /**
     * IllegalArgumentException: 잘못된 인자 (이메일 중복, 기기 등록 오류 등)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {

        log.warn("⚠️ [잘못된 요청] {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * IllegalStateException: 잘못된 상태 (긴급 연락처 없음, 기기 미등록 등)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException e,
            HttpServletRequest request) {

        log.warn("⚠️ [상태 오류] {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * RuntimeException: 긴급 신호 처리 중 오류 등
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException e,
            HttpServletRequest request) {

        log.error("❌ [런타임 오류] {}", e.getMessage(), e);

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Runtime Error")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ===================================
    // 3. 보안 관련 예외
    // ===================================

    /**
     * 로그인 실패 (비밀번호 불일치)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException e,
            HttpServletRequest request) {

        log.warn("⚠️ [로그인 실패] 잘못된 인증 정보");

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("이메일 또는 비밀번호가 일치하지 않습니다")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * 사용자를 찾을 수 없음
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
            UsernameNotFoundException e,
            HttpServletRequest request) {

        log.warn("⚠️ [사용자 없음] {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("존재하지 않는 사용자입니다")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // ===================================
    // 4. Validation 예외
    // ===================================

    /**
     * @Valid 검증 실패 (@NotBlank, @Email 등)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("⚠️ [입력값 검증 실패] {}", errors);

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("입력값 검증에 실패했습니다")
                .path(request.getRequestURI())
                .details(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ===================================
    // 5. 파일 업로드 예외 (음성 파일)
    // ===================================

    /**
     * 파일 크기 초과 (10MB 제한)
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e,
            HttpServletRequest request) {

        log.warn("⚠️ [파일 크기 초과] 최대 10MB까지 업로드 가능");

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error("Payload Too Large")
                .message("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    // ===================================
    // 6. 커스텀 예외 (선택사항)
    // ===================================

    /**
     * 사용자를 찾을 수 없음 (커스텀)
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException e,
            HttpServletRequest request) {

        log.warn("⚠️ [사용자 없음] {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("User Not Found")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * 이메일 중복 (커스텀)
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(
            DuplicateEmailException e,
            HttpServletRequest request) {

        log.warn("⚠️ [이메일 중복] {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Duplicate Email")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * 아두이노 기기 미등록 (커스텀)
     */
    @ExceptionHandler(DeviceNotRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleDeviceNotRegisteredException(
            DeviceNotRegisteredException e,
            HttpServletRequest request) {

        log.warn("⚠️ [기기 미등록] {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Device Not Registered")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * 긴급 신호 처리 오류 (커스텀)
     */
    @ExceptionHandler(EmergencyProcessException.class)
    public ResponseEntity<ErrorResponse> handleEmergencyProcessException(
            EmergencyProcessException e,
            HttpServletRequest request) {

        log.error("❌ [긴급 신호 처리 실패] {}", e.getMessage(), e);

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Emergency Process Failed")
                .message("긴급 신호 처리 중 오류가 발생했습니다: " + e.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}