package com.bosalpim.compozi_ai.domain.document.dto.request.manualFile;

import java.util.List;
import lombok.Getter;

@Getter
public class CreateManualItemDocumentListReqDto {
    private List<CreateManualItemDocumentReqDto> items;
}
