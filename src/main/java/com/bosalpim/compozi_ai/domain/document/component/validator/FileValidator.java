package com.bosalpim.compozi_ai.domain.document.component.validator;

import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.bosalpim.compozi_ai.general.exception.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidator {
    public void validateDocumentFile(MultipartFile file) {

        // 1. 파일 존재 여부 검증
        if (file == null || file.isEmpty()) {
            throw new CustomException(BadStatusCode.FILE_PARSE_FAILED);
        }

        // 2. 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".csv"))
                        && !originalFilename.endsWith(".png") && !originalFilename.endsWith(".pdf")) {
            throw new CustomException(BadStatusCode.UNSUPPORTED_FILE_TYPE);
        }
    }

}
