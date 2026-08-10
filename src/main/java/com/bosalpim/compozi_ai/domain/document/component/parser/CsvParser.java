package com.bosalpim.compozi_ai.domain.document.component.parser;

import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import com.opencsv.CSVReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

                ParseValueHelper.ParseContext context = new ParseValueHelper.ParseContext();

                CreateCommonItemDocumentReqDto dto = CreateCommonItemDocumentReqDto.builder()
                        .rowNo((long) i)
                        .docId(ParseValueHelper.parseString(getValue(row, 0)))
                        .sourceType(ParseValueHelper.parseString(getValue(row, 1)))
                        .supplierName(ParseValueHelper.parseString(getValue(row, 2)))
                        .rawItemName(ParseValueHelper.parseString(getValue(row, 3)))
                        .spec(ParseValueHelper.parseString(getValue(row, 4)))
                        .unit(ParseValueHelper.parseString(getValue(row, 5)))
                        .priceBefore(ParseValueHelper.parseLong(getValue(row, 6), context))
                        .priceAfter(ParseValueHelper.parseLong(getValue(row, 7), context))
                        .effectiveDate(ParseValueHelper.parseDate(getValue(row, 8), context))
                        .hasParseError(context.hasError())
                        .build();

                list.add(dto);
            }

        } catch (Exception e) {
            throw new CustomException(BadStatusCode.FILE_PARSE_FAILED);
        }

        return list;
    }

    private String getValue(String[] row, int index) {
        return (index < row.length) ? row[index] : null;
    }


}
