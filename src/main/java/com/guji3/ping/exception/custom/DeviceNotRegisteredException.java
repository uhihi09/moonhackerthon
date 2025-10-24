package com.guji3.ping.exception.custom;

/**
 * 아두이노 기기가 등록되지 않았을 때 발생하는 예외
 */
public class DeviceNotRegisteredException extends RuntimeException {

    public DeviceNotRegisteredException(String deviceSerial) {
        super(String.format("등록되지 않은 기기입니다: %s", deviceSerial));
    }
}