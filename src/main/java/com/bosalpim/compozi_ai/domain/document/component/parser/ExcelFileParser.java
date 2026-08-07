package com.bosalpim.compozi_ai.domain.document.component.parser;

import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelFileParser implements FileParser {

    @Override
    public boolean supports(String filename) {
        return filename != null && filename.endsWith(".xlsx");
    }

    @Override
    public List<CreateCommonItemDocumentReqDto> parse(MultipartFile file) {
        List<CreateCommonItemDocumentReqDto> list = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // 헤더 제외
                }

                CreateCommonItemDocumentReqDto dto = CreateCommonItemDocumentReqDto.builder()
                        .rowNo((long) row.getRowNum())
                        .docId(getStringValue(row.getCell(0)))
                        .sourceType(getStringValue(row.getCell(1)))
                        .supplierName(getStringValue(row.getCell(2)))
                        .rawItemName(getStringValue(row.getCell(3)))
                        .spec(getStringValue(row.getCell(4)))
                        .unit(getStringValue(row.getCell(5)))
                        .priceBefore(getLongValue(row.getCell(6)))
                        .priceAfter(getLongValue(row.getCell(7)))
                        .effectiveDate(getDateValue(row.getCell(8)))
                        .build();

                list.add(dto);
            }


        } catch (Exception e) {
            throw new CustomException(BadStatusCode.FILE_PARSE_FAILED);
        }
        return list;
    }


    private String getStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> null;
        };
    }


    private Long getLongValue(Cell cell) {
        // 1. 엑셀 셀 포맷이 '숫자'인 경우 (30000)
        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        }

        // 2. 엑셀 셀 포맷이 '텍스트'인 경우 ("30,000" 또는 "30000")
        if (cell.getCellType() == CellType.STRING) {
            String text = cell.getStringCellValue().trim();
            if (text.isBlank()) {
                return null;
            }

            // 콤마(,) 제거 후 숫자 파싱
            String cleanText = text.replace(",", "");
            return Long.parseLong(cleanText);
        }

        return null;
    }

    private LocalDate getDateValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        // 1. 엑셀 실제 날짜 서식인 경우 (NUMERIC)
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        // 2. 텍스트 형태로 "2026-08-01" 또는 "2026/08/01" 등이 입력된 경우 (STRING)
        if (cell.getCellType() == CellType.STRING) {
            String text = cell.getStringCellValue().trim();
            if (text.isBlank()) {
                return null;
            }

            // "2026/08/01" 형태를 "2026-08-01"로 통일 후 파싱
            text = text.replace("/", "-");
            return LocalDate.parse(text); // YYYY-MM-DD 포맷 파싱
        }

        // 3. 조건에 맞는 날짜 데이터가 아닌 경우 기본 반환
        return null;
    }


}
