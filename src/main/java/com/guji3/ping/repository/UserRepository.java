package com.guji3.ping.repository;

import com.guji3.ping.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByDeviceSerial(String deviceSerial);
    boolean existsByEmail(String email);
    boolean existsByDeviceSerial(String deviceSerial);
}