package com.bosalpim.compozi_ai.domain.document.component.ocr;

import com.bosalpim.compozi_ai.domain.document.component.ocr.ParseOcrValueHelper.ParseContext;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.enums.SourceType;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface OcrSubParser {
    // OCR 결과 텍스트를 보고 본인이 처리할 양식인지 판단
    boolean supports(List<String> ocrLines);

    // 실제 파싱 수행
    List<CreateCommonItemDocumentReqDto> parse(
            List<String> ocrLines,
            MultipartFile file,
            ParseContext context,
            SourceType sourceType
    );
}
