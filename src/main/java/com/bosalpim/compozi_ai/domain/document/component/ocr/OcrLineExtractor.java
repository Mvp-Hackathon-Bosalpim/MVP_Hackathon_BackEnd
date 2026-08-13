package com.bosalpim.compozi_ai.domain.document.component.ocr;

import com.bosalpim.compozi_ai.domain.document.dto.response.ClovaOcrGeneralResponseDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;


// OCR 라인 하나하나 가져옴 ("lineBreak" 값이 true 인 경우 줄바꿈)
@Component
public class OcrLineExtractor {

    public static List<String> extractLines(List<ClovaOcrGeneralResponseDto.Field> fields) {
        if (fields == null || fields.isEmpty()) {
            return List.of();
        }

        List<String> resultLines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (ClovaOcrGeneralResponseDto.Field field : fields) {
            if (field == null || field.inferText() == null || field.inferText().isBlank()) {
                continue;
            }

            // 1. 이미 텍스트가 있다면 띄어쓰기 한 칸 추가하고 연결
            if (!currentLine.isEmpty()) {
                currentLine.append(" ");
            }
            currentLine.append(field.inferText().trim());

            // 2. Clova가 개행(lineBreak)으로 판정한 항목이면 줄 완성
            if (Boolean.TRUE.equals(field.lineBreak())) {
                resultLines.add(currentLine.toString().trim());
                currentLine.setLength(0);
            }
        }

        // 남은 텍스트 처리
        if (!currentLine.isEmpty()) {
            resultLines.add(currentLine.toString().trim());
        }

        return resultLines;
    }
}
