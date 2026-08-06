package com.bosalpim.compozi_ai.domain.item.dto.response.manual;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateManualItemDocumentResDto {
    @JsonProperty("file_id")
    private final List<Long> fileId;
}
