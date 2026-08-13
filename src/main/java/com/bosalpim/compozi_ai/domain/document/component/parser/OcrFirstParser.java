package com.bosalpim.compozi_ai.domain.document.component.parser;

import com.bosalpim.compozi_ai.domain.document.component.parser.ParseOcrValueHelper.ParseContext;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.enums.SourceType;
import com.bosalpim.compozi_ai.domain.document.service.ClovaOcrService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Slf4j
public class OcrFirstParser implements FileParser { // 44_ 형태 pdf 및 이미지 입력 (형태 1)

    private final ClovaOcrService ocrService;

    private static final Pattern DOC_ID_PATTERN = Pattern.compile("(?:문서번호|거래명세서|No\\.)\\s*[:\\=\\-]?\\s*(\\S+)");


    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}[.-]\\d{2}[.-]\\d{2}$");

    @Override
    public boolean supports(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".pdf") || lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    @Override
    public List<CreateCommonItemDocumentReqDto> parse(MultipartFile file) throws IOException {
        List<String> lines = ocrService.processGeneralOcrToGrid(file);

        // 1. 파일 확장자 기반으로 SourceType 동적 결정
        SourceType sourceType = determineSourceType(file.getOriginalFilename());

        // 2. 파싱 에러/로그 추적용 Context 생성
        ParseContext context = new ParseContext();

        // 3. 텍스트 라인 기반 DTO 생성 (sourceType 전달)
        return parseToDtos(lines, context, sourceType);
    }

    public static List<CreateCommonItemDocumentReqDto> parseToDtos(List<String> lines, ParseContext context,
                                                                   SourceType sourceType) {
        List<CreateCommonItemDocumentReqDto> resultList = new ArrayList<>();

        if (lines == null || lines.isEmpty()) {
            return resultList;
        }

        // 문서 상단에서 공통 정보 추출
        String supplierName = extractSupplierName(lines);
        log.info("공급 업체 명 : {}", supplierName);

        String docId = extractDocId(lines);
        log.info("문서 id : {}", docId);

        int tableHeaderEndIndex = findTableHeaderEndIndex(lines);
        log.info("table 헤더 끝 인덱스 : {}", tableHeaderEndIndex);

        if (tableHeaderEndIndex == -1) {
            return resultList;
        }

        List<String> tableTokens = new ArrayList<>();
        // 표 헤더 종료 바로 다음 줄부터 데이터 행 파싱 시작
        for (int i = tableHeaderEndIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // 표 종료 조건 (하단 비고, 감사 문구, 대표이사 등)
            if (line.startsWith("◯") || line.startsWith("앞으로도") || line.contains("대표이사")) {
                break;
            }

            if (line.isBlank()) {
                continue;
            }

            // 공백 기준으로 분할하여 토큰 리스트에 순서대로 담기
            String[] parts = line.split("\\s+");
            for (String part : parts) {
                if (!part.isBlank()) {
                    tableTokens.add(part);
                }
            }
        }

        long rowNo = 1;
        int subIndex = 1;
        int idx = 0;

        // 날짜 형식 검증용 정규식 패턴 (YYYY-MM-DD 또는 YYYY.MM.DD)
        while (idx < tableTokens.size()) {
            // 1개 Row를 구성할 토큰 수집
            List<String> rowTokens = new ArrayList<>();

            while (idx < tableTokens.size()) {
                String token = tableTokens.get(idx++);
                rowTokens.add(token);

                // 1. 날짜 패턴을 만나면 1개 Row 구성 완료
                if (DATE_PATTERN.matcher(token).matches()) {
                    break;
                }

                // 2. 날짜가 없더라도 이미 5개 이상 수집되었는데 다음 토큰이 없거나,
                // 다음 토큰이 날짜가 아닌 새로운 행의 품목으로 보이는 경우 (최대 6개 방어선)
                if (rowTokens.size() == 5) {
                    // 다음 토큰이 날짜 패턴이 아니면 적용일자가 빠진 Row로 판단하고 5개에서 끊음
                    if (idx < tableTokens.size() && !DATE_PATTERN.matcher(tableTokens.get(idx)).matches()) {
                        break;
                    }
                }
            }

            // 최소 품목 데이터 형태(4개 이상)를 갖춘 경우 DTO 생성
            if (rowTokens.size() >= 4) {
                String rawItemName = rowTokens.get(0);
                String rawSpec = rowTokens.size() > 1 ? rowTokens.get(1) : "";
                String unit = rowTokens.size() > 2 ? rowTokens.get(2) : "";
                String priceBeforeStr = rowTokens.size() > 3 ? rowTokens.get(3) : "0";
                String priceAfterStr = rowTokens.size() > 4 ? rowTokens.get(4) : "0";

                // 날짜 토큰이 존재하는 경우만 가져오고 없으면 null 처리
                String effectiveDateStr = (rowTokens.size() >= 6 && DATE_PATTERN.matcher(rowTokens.get(5)).matches())
                        ? rowTokens.get(5) : null;

                String formattedDocId = String.format("%s-%03d", docId, subIndex++);

                log.info("Row #{}: item={}, spec={}, unit={}, priceBefore={}, priceAfter={}, date={}",
                        rowNo, rawItemName, rawSpec, unit, priceBeforeStr, priceAfterStr, effectiveDateStr);

                CreateCommonItemDocumentReqDto dto = CreateCommonItemDocumentReqDto.builder()
                        .rowNo(rowNo++)
                        .docId(formattedDocId)
                        .sourceType(sourceType.name())
                        .supplierName(supplierName)
                        .rawItemName(rawItemName)
                        .spec(rawSpec)
                        .unit(unit)
                        .priceBefore(ParseOcrValueHelper.parseLong(priceBeforeStr, context))
                        .priceAfter(ParseOcrValueHelper.parseLong(priceAfterStr, context))
                        .effectiveDate(
                                effectiveDateStr != null ? ParseOcrValueHelper.parseDate(effectiveDateStr, context)
                                        : null)
                        .hasParseError(context.hasError())
                        .build();

                resultList.add(dto);
            }
        }

        return resultList;
    }

    // 파일 확장자로 SourceType 판별하는 헬퍼 메서드
    private SourceType determineSourceType(String filename) {
        if (filename == null) {
            return SourceType.PDF; // 기본값 설정
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return SourceType.IMAGE;
        }
        return SourceType.PDF;
    }

    // 1. 문서번호 동적 추출
    private static String extractDocId(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = DOC_ID_PATTERN.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "UNKNOWN_DOC_ID";
    }

    // 2. 공급업체명 추출 (수정: 전체 라인 탐색)
    private static String extractSupplierName(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "UNKNOWN";
        }

        for (String line : lines) {
            String cleaned = line.replaceAll("\\s+", "");
            if (!cleaned.isBlank()) {
                return cleaned;
            }
        }

        return "UNKNOWN";
    }

    // 3. 표 헤더 영역 탐색 (단일 줄 / 다중 줄 헤더 모두 대응)
    private static int findTableHeaderEndIndex(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String cleaned = lines.get(i).replaceAll("\\s+", "");

            // 한 줄에 '품목'과 '적용일자'가 다 포함된 경우
            if (cleaned.contains("품목") && (cleaned.contains("적용일자") || cleaned.contains("변경단가"))) {
                return i;
            }

            // '적용일자'가 마지막 헤더 항목으로 별도 줄로 올 경우
            if (cleaned.equals("적용일자")) {
                return i;
            }
        }
        return -1;
    }
}
