package com.bosalpim.compozi_ai.domain.document.service;

import com.bosalpim.compozi_ai.domain.document.dto.response.ClovaOcrGeneralResponseDto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClovaOcrService {

    @Value("${clova.ocr.invoke-url}")
    private String invokeUrl;

    @Value("${clova.ocr.secret-key}")
    private String secretKey;

    private final RestClient restClient;

    public ClovaOcrService() {
        this.restClient = RestClient.create();
    }

    public List<List<String>> processGeneralOcrToGrid(MultipartFile file) throws IOException {
        String base64Data = Base64.getEncoder().encodeToString(file.getBytes());

        Map<String, Object> messageJson = Map.of(
                "version", "V2",
                "requestId", UUID.randomUUID().toString(),
                "timestamp", System.currentTimeMillis(),
                "images", List.of(Map.of(
                        "format", getValidFormat(file.getOriginalFilename()),
                        "name", "table_image",
                        "data", base64Data
                ))
        );

        ClovaOcrGeneralResponseDto response = restClient.post()
                .uri(invokeUrl)
                .header("X-OCR-SECRET", secretKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(messageJson)
                .retrieve()
                .body(ClovaOcrGeneralResponseDto.class);

        if (response == null || response.images() == null || response.images().isEmpty()) {
            return Collections.emptyList();
        }

        List<ClovaOcrGeneralResponseDto.Field> fields = response.images().get(0).fields();
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }

        return convertFieldsToGrid(fields);
    }

    private List<List<String>> convertFieldsToGrid(List<ClovaOcrGeneralResponseDto.Field> fields) {
        List<List<ClovaOcrGeneralResponseDto.Field>> rowContainers = new ArrayList<>();

        // Y좌표 차이가 18px 이내면 동일한 행(Row)으로 묶음
        for (ClovaOcrGeneralResponseDto.Field field : fields) {
            if (field == null || field.inferText() == null || field.inferText().isBlank()) {
                continue;
            }

            double y = field.boundingPoly().vertices().get(0).y();
            boolean matched = false;

            for (List<ClovaOcrGeneralResponseDto.Field> row : rowContainers) {
                double rowY = row.get(0).boundingPoly().vertices().get(0).y();
                if (Math.abs(rowY - y) <= 18.0) {
                    row.add(field);
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                List<ClovaOcrGeneralResponseDto.Field> newRow = new ArrayList<>();
                newRow.add(field);
                rowContainers.add(newRow);
            }
        }

        // Y좌표 오름차순 (위 -> 아래)
        rowContainers.sort(Comparator.comparingDouble(r -> r.get(0).boundingPoly().vertices().get(0).y()));

        List<List<String>> grid = new ArrayList<>();

        for (List<ClovaOcrGeneralResponseDto.Field> row : rowContainers) {
            // X좌표 오름차순 (좌 -> 우)
            row.sort(Comparator.comparingDouble(f -> f.boundingPoly().vertices().get(0).x()));

            List<String> textRow = new ArrayList<>();
            for (ClovaOcrGeneralResponseDto.Field field : row) {
                String text = field.inferText().trim();
                if (!text.isEmpty()) {
                    textRow.add(text);
                }
            }

            if (!textRow.isEmpty()) {
                grid.add(textRow);
            }
        }

        return grid;
    }

    private String getValidFormat(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "png";
        }
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "jpeg", "jpg" -> "jpg";
            case "pdf" -> "pdf";
            case "tif", "tiff" -> "tiff";
            default -> "png";
        };
    }
}
