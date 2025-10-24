package com.guji3.ping.service;

import com.guji3.ping.dto.UserRegisterDto;
import com.guji3.ping.entity.User;
import com.guji3.ping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public User registerUser(UserRegisterDto dto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + dto.getEmail());
        }

        // 기기 시리얼 중복 체크 (있는 경우에만)
        if (dto.getDeviceSerial() != null &&
                userRepository.existsByDeviceSerial(dto.getDeviceSerial())) {
            throw new IllegalArgumentException("이미 등록된 기기입니다: " + dto.getDeviceSerial());
        }

        // User 엔티티 생성
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // 비밀번호 암호화
                .name(dto.getName())
                .phone(dto.getPhone())
                .deviceSerial(dto.getDeviceSerial())
                .build();

        User savedUser = userRepository.save(user);
        log.info("✅ 회원가입 완료: {} (ID: {})", savedUser.getEmail(), savedUser.getUserId());

        return savedUser;
    }

    /**
     * 이메일로 사용자 찾기
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + email));
    }

    /**
     * 기기 시리얼로 사용자 찾기 (아두이노 연동용)
     */
    public User findByDeviceSerial(String deviceSerial) {
        return userRepository.findByDeviceSerial(deviceSerial)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 기기입니다: " + deviceSerial));
    }

    /**
     * 비밀번호 검증
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 아두이노 기기 등록/변경
     */
    @Transactional
    public User updateDeviceSerial(Long userId, String deviceSerial) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        // 다른 사용자가 이미 사용 중인지 확인
        userRepository.findByDeviceSerial(deviceSerial).ifPresent(existingUser -> {
            if (!existingUser.getUserId().equals(userId)) {
                throw new IllegalArgumentException("다른 사용자가 이미 사용 중인 기기입니다");
            }
        });

        user.setDeviceSerial(deviceSerial);
        log.info("🔧 기기 등록: 사용자 {} - 기기 {}", userId, deviceSerial);

        return user;
    }
}