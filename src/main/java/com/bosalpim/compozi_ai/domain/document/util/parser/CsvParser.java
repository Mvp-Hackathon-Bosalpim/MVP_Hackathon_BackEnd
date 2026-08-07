package com.bosalpim.compozi_ai.domain.document.util.parser;

import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import com.opencsv.CSVReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CsvParser implements FileParser {
    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }

    @Override
    public List<CreateCommonItemDocumentReqDto> parse(MultipartFile file) {
        List<CreateCommonItemDocumentReqDto> list = new ArrayList<>();

        try (InputStreamReader inputStreamReader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(inputStreamReader)) {

            List<String[]> rows = csvReader.readAll();
            if (rows.isEmpty()) {
                return list;
            }

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);

                // 빈 행 스킵
                if (row.length == 0 || (row.length == 1 && row[0].isBlank())) {
                    continue;
                }

                CreateCommonItemDocumentReqDto dto = CreateCommonItemDocumentReqDto.builder()
                        .rowNo((long) i)
                        .docId(getStringValue(row, 0))
                        .sourceType(getStringValue(row, 1))
                        .supplierName(getStringValue(row, 2))
                        .rawItemName(getStringValue(row, 3))
                        .spec(getStringValue(row, 4))
                        .unit(getStringValue(row, 5))
                        .priceBefore(getLongValue(row, 6))
                        .priceAfter(getLongValue(row, 7))
                        .effectiveDate(getDateValue(row, 8))
                        .build();

                list.add(dto);
            }

        } catch (Exception e) {
            throw new CustomException(BadStatusCode.FILE_PARSE_FAILED);
        }

        return list;
    }

    // Array IndexOutOfBoundsException 방지 및 문자열 정제
    private String getStringValue(String[] row, int index) {
        if (index >= row.length || row[index] == null) {
            return null;
        }
        String val = row[index].trim();
        return val.isBlank() ? null : val;
    }

    // Long 숫자로 변환 (콤마 제거)
    private Long getLongValue(String[] row, int index) {
        String val = getStringValue(row, index);
        if (val == null) {
            return null;
        }
        return Long.parseLong(val.replace(",", ""));
    }

    // LocalDate 날짜로 변환 (2026-08-01 또는 2026/08/01)
    private LocalDate getDateValue(String[] row, int index) {
        String val = getStringValue(row, index);
        if (val == null) {
            return null;
        }
        return LocalDate.parse(val.replace("/", "-"));
    }
}
