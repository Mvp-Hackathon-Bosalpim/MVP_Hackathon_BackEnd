package com.bosalpim.compozi_ai.domain.document.component.parser;

import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface FileParser {

    boolean supports(String filename);


    List<CreateCommonItemDocumentReqDto> parse(MultipartFile file) throws IOException;

    default boolean isOcrParser() {
        return false;
    }
}
