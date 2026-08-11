package com.bosalpim.compozi_ai.domain.document.component.parser;

import com.bosalpim.compozi_ai.domain.document.component.parser.ParseOcrValueHelper.ParseContext;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.service.ClovaOcrService;
import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class OcrParser implements FileParser {

    private final ClovaOcrService clovaOcrService;

    @Override
    public boolean supports(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".pdf") || lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    @Override
    public List<CreateCommonItemDocumentReqDto> parse(MultipartFile file) {
        List<CreateCommonItemDocumentReqDto> list = new ArrayList<>();

        try {
            List<List<String>> rows = clovaOcrService.processGeneralOcrToGrid(file);

            if (rows == null || rows.isEmpty()) {
                return list;
            }

            for (int i = 0; i < rows.size(); i++) {
                List<String> row = rows.get(i);

                if (row == null || row.isEmpty()) {
                    continue;
                }

                // 헤더 행(문서ID, 원본유형 등) 스킵
                if (row.get(0).contains("문서ID") || row.get(0).contains("원본유형")) {
                    continue;
                }

                ParseContext context = new ParseContext();

                // 스마트 파싱: 쪼개진 텍스트들을 9개 컬럼 구조로 조합
                ParsedRow parsedRow = parseRowSmartly(row);

                String formattedSpec = OcrNormalizeUtils.formatSpecWithMultiplySymbol(parsedRow.spec);

                CreateCommonItemDocumentReqDto dto = CreateCommonItemDocumentReqDto.builder()
                        .rowNo((long) (i + 1))
                        .docId(parsedRow.docId)
                        .sourceType(parsedRow.sourceType)
                        .supplierName(parsedRow.supplierName)
                        .rawItemName(parsedRow.rawItemName)
                        .spec(formattedSpec)
                        .unit(parsedRow.unit)
                        .priceBefore(ParseOcrValueHelper.parseLong(parsedRow.priceBefore, context))
                        .priceAfter(ParseOcrValueHelper.parseLong(parsedRow.priceAfter, context))
                        .effectiveDate(ParseOcrValueHelper.parseDate(parsedRow.effectiveDate, context))
                        .hasParseError(context.hasError())
                        .build();

                list.add(dto);
            }

        } catch (Exception e) {
            throw new CustomException(BadStatusCode.FILE_PARSE_FAILED);
        }

        return list;
    }


    private ParsedRow parseRowSmartly(List<String> row) {
        ParsedRow result = new ParsedRow();

        if (row.isEmpty()) {
            return result;
        }

        // 1. 앞쪽 고정값 채우기 (문서ID, 원본유형, 공급사)
        result.docId = getValueSafe(row, 0);
        result.sourceType = getValueSafe(row, 1);
        result.supplierName = getValueSafe(row, 2);

        // 2. 뒤쪽 고정값 채우기 (날짜, 변경단가, 기존단가, 단위)
        int size = row.size();
        int lastIdx = size - 1;

        // 적용일자 (2026-08-01 형태인지 확인)
        if (lastIdx >= 3 && row.get(lastIdx).matches("\\d{4}-\\d{2}-\\d{2}")) {
            result.effectiveDate = row.get(lastIdx);
            lastIdx--;
        }

        // 변경단가 (숫자/콤마 형태)
        if (lastIdx >= 3 && isNumericOrPrice(row.get(lastIdx))) {
            result.priceAfter = row.get(lastIdx);
            lastIdx--;
        }

        // 기존단가 (숫자/콤마 형태)
        if (lastIdx >= 3 && isNumericOrPrice(row.get(lastIdx))) {
            result.priceBefore = row.get(lastIdx);
            lastIdx--;
        }

        // 단위 (BOX, PK, PO, KG/단 등)
        if (lastIdx >= 3) {
            result.unit = row.get(lastIdx);
            lastIdx--;
        }

        // 3. 중간에 남은 토큰들 (원문 품목명 + 규격) 처리
        if (3 <= lastIdx) {
            // 품목명은 첫 번째 남은 토큰
            result.rawItemName = row.get(3);

            // 4번 인덱스부터 lastIdx까지 남아있는 모든 토큰을 하나로 연결하여 spec(규격) 생성
            // 예: ["기존", "10kg", "/", "변경", "9kg"] -> "기존 10kg / 변경 9kg"
            StringBuilder specBuilder = new StringBuilder();
            for (int k = 4; k <= lastIdx; k++) {
                if (specBuilder.length() > 0) {
                    specBuilder.append(" ");
                }
                specBuilder.append(row.get(k));
            }
            result.spec = specBuilder.toString();
        }

        return result;
    }

    private boolean isNumericOrPrice(String str) {
        if (str == null) {
            return false;
        }
        String clean = str.replace(",", "").trim();
        try {
            Long.parseLong(clean);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getValueSafe(List<String> row, int index) {
        return (index < row.size()) ? row.get(index) : "";
    }

    private static class ParsedRow {
        String docId = "";
        String sourceType = "";
        String supplierName = "";
        String rawItemName = "";
        String spec = "";
        String unit = "";
        String priceBefore = "";
        String priceAfter = "";
        String effectiveDate = "";
    }
}
