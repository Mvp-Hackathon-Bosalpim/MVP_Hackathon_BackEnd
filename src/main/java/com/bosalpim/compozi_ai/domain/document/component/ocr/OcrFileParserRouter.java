package com.bosalpim.compozi_ai.domain.document.component.ocr;

import com.bosalpim.compozi_ai.domain.document.component.ocr.ParseOcrValueHelper.ParseContext;
import com.bosalpim.compozi_ai.domain.document.component.parser.FileParser;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.enums.SourceType;
import com.bosalpim.compozi_ai.domain.document.service.ClovaOcrService;
import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrFileParserRouter implements FileParser {

    private final ClovaOcrService ocrService;
    private final List<OcrSubParser> subParsers;

    @Override
    public boolean supports(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        // PDF 및 이미지 확장자 지원
        return lower.endsWith(".pdf") || lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }

    @Override
    public List<CreateCommonItemDocumentReqDto> parse(MultipartFile file) throws IOException {
        // 1. 공통 OCR 호출 (1회만 수행)
        List<String> ocrLines = ocrService.processGeneralOcrToGrid(file);

        SourceType sourceType = determineSourceType(file.getOriginalFilename());
        ParseContext context = new ParseContext();

        // 2. supports() 매칭을 통한 서브 파서 선택 알고리즘
        OcrSubParser matchedParser = subParsers.stream()
                .filter(subParser -> subParser.supports(ocrLines))
                .findFirst()
                .orElseThrow(() -> new CustomException(BadStatusCode.FILE_PARSE_FAILED));

        log.info("매칭된 OCR 파서 클래스: {}", matchedParser.getClass().getSimpleName());

        // 3. 선택된 SubParser에 파싱 위임
        return matchedParser.parse(ocrLines, file, context, sourceType);
    }

    private SourceType determineSourceType(String filename) {
        if (filename == null) {
            return SourceType.PDF;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return SourceType.IMAGE;
        }
        return SourceType.PDF;
    }
}
