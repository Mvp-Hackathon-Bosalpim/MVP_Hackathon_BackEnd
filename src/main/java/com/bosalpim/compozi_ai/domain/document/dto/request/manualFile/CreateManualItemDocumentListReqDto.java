package com.bosalpim.compozi_ai.domain.document.dto.request.manualFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;

@Getter
public class CreateManualItemDocumentListReqDto {
    @Valid
    @NotEmpty(message = "품목 리스트는 최소 1개 이상이어야 합니다.")
    private List<CreateManualItemDocumentReqDto> items;
}
