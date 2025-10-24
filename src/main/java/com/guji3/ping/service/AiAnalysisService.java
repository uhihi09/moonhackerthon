package com.guji3.ping.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * 음성 파일 → 텍스트 변환 (OpenAI Whisper API)
     */
    public String transcribeAudio(MultipartFile audioFile) throws Exception {
        log.info("🎤 음성 인식 시작: 파일명 {}, 크기 {} bytes",
                audioFile.getOriginalFilename(), audioFile.getSize());

        // 임시 파일로 저장
        File tempFile = File.createTempFile("audio_", ".mp3");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(audioFile.getBytes());
        }

        try {
            // Whisper API 호출
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            byte[] audioBytes = Files.readAllBytes(tempFile.toPath());

            String body = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"audio.mp3\"\r\n" +
                    "Content-Type: audio/mpeg\r\n\r\n" +
                    new String(audioBytes) + "\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"model\"\r\n\r\n" +
                    "whisper-1\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"language\"\r\n\r\n" +
                    "ko\r\n" +
                    "--" + boundary + "--\r\n";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/audio/transcriptions"))
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            Map<String, Object> result = gson.fromJson(response.body(), Map.class);
            String transcribedText = (String) result.get("text");

            log.info("✅ 음성 인식 완료: {}", transcribedText);
            return transcribedText;

        } finally {
            tempFile.delete(); // 임시 파일 삭제
        }
    }

    /**
     * GPT-4로 상황 분석
     */
    public Map<String, String> analyzeSituation(String audioText) throws Exception {
        log.info("🧠 GPT 상황 분석 시작: {}", audioText);

        String prompt = String.format(
                "다음은 긴급 구조 요청 기기에서 수집한 음성 데이터입니다:\n\n" +
                        "\"%s\"\n\n" +
                        "이 상황을 분석하여 다음 정보를 JSON 형식으로 제공해주세요:\n" +
                        "1. situation: 어떤 위험 상황인지 (예: 납치, 강도, 실종, 사고 등)\n" +
                        "2. dangerLevel: 위험도 (HIGH/MEDIUM/LOW)\n" +
                        "3. analysis: 상황에 대한 자세한 분석 (50자 이내)\n" +
                        "4. recommendAction: 보호자가 취해야 할 행동 (30자 이내)\n\n" +
                        "음성이 비어있거나 명확하지 않으면 dangerLevel을 LOW로 설정하고 " +
                        "situation을 '상황 불명확'으로 표시해주세요.",
                audioText
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", "당신은 긴급 상황을 분석하는 AI 어시스턴트입니다. 반드시 JSON 형식으로만 답변하세요."),
                Map.of("role", "user", "content", prompt)
        });
        requestBody.put("temperature", 0.3); // 일관성 있는 답변을 위해 낮은 온도

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        Map<String, Object> result = gson.fromJson(response.body(), Map.class);
        Map<String, Object> choice = (Map<String, Object>)
                ((java.util.List<?>) result.get("choices")).get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");
        String content = (String) message.get("content");

        // JSON 파싱
        Map<String, String> analysis = gson.fromJson(content, Map.class);

        log.info("✅ GPT 분석 완료: 상황={}, 위험도={}",
                analysis.get("situation"), analysis.get("dangerLevel"));

        return analysis;
    }

    /**
     * 통합 분석 (음성 인식 + GPT 분석)
     */
    public Map<String, String> fullAnalysis(MultipartFile audioFile) throws Exception {
        // 1단계: 음성 → 텍스트
        String audioText = transcribeAudio(audioFile);

        // 2단계: GPT 상황 분석
        Map<String, String> analysis = analyzeSituation(audioText);
        analysis.put("audioText", audioText); // 원본 텍스트 포함

        return analysis;
    }
}