package com.bosalpim.compozi_ai.domain.document.component.parser;

import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
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
                if (row.getRowNum() == 0 || isRowEmpty(row)) {
                    continue; // 헤더 제외 및 빈 행 제외
                }

                ParseXlsxValueHelper.ParseContext context = new ParseXlsxValueHelper.ParseContext();
                CreateCommonItemDocumentReqDto dto = CreateCommonItemDocumentReqDto.builder()
                        .rowNo((long) row.getRowNum())
                        .docId(ParseXlsxValueHelper.parseString(row.getCell(0)))
                        .sourceType(ParseXlsxValueHelper.parseString(row.getCell(1)))
                        .supplierName(ParseXlsxValueHelper.parseString(row.getCell(2)))
                        .rawItemName(ParseXlsxValueHelper.parseString(row.getCell(3)))
                        .spec(ParseXlsxValueHelper.parseString(row.getCell(4)))
                        .unit(ParseXlsxValueHelper.parseString(row.getCell(5)))
                        .priceBefore(ParseXlsxValueHelper.parseLong(row.getCell(6), context))
                        .priceAfter(ParseXlsxValueHelper.parseLong(row.getCell(7), context))
                        .effectiveDate(ParseXlsxValueHelper.parseDate(row.getCell(8), context))
                        .hasParseError(context.hasError())
                        .build();

                list.add(dto);
            }


        } catch (Exception e) {
            throw new CustomException(BadStatusCode.FILE_PARSE_FAILED);
        }
        return list;
    }


    // 빈 행(Blank Row) 스킵용 메서드
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }


}
