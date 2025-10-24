package com.guji3.ping.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * GPS 좌표 → 주소 변환 (Google Maps Geocoding API)
     */
    public String getAddressFromCoordinates(BigDecimal latitude, BigDecimal longitude) {
        try {
            if (googleMapsApiKey == null || googleMapsApiKey.isEmpty()) {
                log.warn("⚠️ Google Maps API Key가 설정되지 않음. 좌표만 반환합니다.");
                return String.format("위도: %s, 경도: %s", latitude, longitude);
            }

            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=%s&language=ko",
                    latitude, longitude, googleMapsApiKey
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            Map<String, Object> result = gson.fromJson(response.body(), Map.class);

            if ("OK".equals(result.get("status"))) {
                java.util.List<Map<String, Object>> results =
                        (java.util.List<Map<String, Object>>) result.get("results");

                if (!results.isEmpty()) {
                    String address = (String) results.get(0).get("formatted_address");
                    log.info("📍 주소 변환 완료: {}", address);
                    return address;
                }
            }

            log.warn("⚠️ 주소 변환 실패. 좌표 반환: {}, {}", latitude, longitude);
            return String.format("위도: %s, 경도: %s", latitude, longitude);

        } catch (Exception e) {
            log.error("❌ 주소 변환 오류", e);
            return String.format("위도: %s, 경도: %s", latitude, longitude);
        }
    }

    /**
     * 간단한 좌표 검증
     */
    public boolean isValidCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }

        // 대한민국 좌표 범위 대략 검증
        // 위도: 33.0 ~ 38.6, 경도: 124.0 ~ 132.0
        return latitude.compareTo(BigDecimal.valueOf(33.0)) >= 0 &&
                latitude.compareTo(BigDecimal.valueOf(38.6)) <= 0 &&
                longitude.compareTo(BigDecimal.valueOf(124.0)) >= 0 &&
                longitude.compareTo(BigDecimal.valueOf(132.0)) <= 0;
    }
}