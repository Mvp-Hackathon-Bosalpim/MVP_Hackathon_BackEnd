package com.bosalpim.compozi_ai.domain.item.dto.request.manual;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateManualItemDocumentListReqDto {
    private List<CreateManualItemDocumentReqDto> items;
}
