package com.bosalpim.compozi_ai.domain.document.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GeminiNormalizationService {


    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder().build();

    public Optional<String> normalizeWithAi(String rawName) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key="
                + apiKey;

        String prompt = String.format(
                "너는 식품 및 식자재 품목명 정규화 전문가야.\n" +
                        "입력된 품목명에서 용량, 수량, 규격 단위(예: 10K, 30입, 500g 등)를 제거하고 표준 한글 품목명만 추출해줘.\n" +
                        "약어가 있다면 전개하고(예: BBQ->바비큐, S/O->소스), 띄어쓰기를 바르게 교정해줘.\n" +
                        "부연 설명이나 인사말은 절대 하지 말고, 오직 '정규화된 품목명'만 단답형으로 응답해줘.\n" +
                        "그리고 줄임말과 같은것은 풀어서 써줘 (예 : 냉감튀 -> 냉동 감자 튀김)\n\n" +
                        "입력 품목명: %s", rawName
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        try {
            Map response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // 동기 처리

            // 응답 파싱
            List candidates = (List) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                List parts = (List) content.get("parts");
                Map part = (Map) parts.get(0);

                String aiResult = ((String) part.get("text")).trim();
                return Optional.ofNullable(aiResult);
            }
        } catch (Exception e) {
            // AI 호출 실패 시 로그 남기고 빈 값 반환 (시스템 중단 방지)
            System.err.println("Gemini API 호출 실패: " + e.getMessage());
        }

        return Optional.empty();
    }
}
