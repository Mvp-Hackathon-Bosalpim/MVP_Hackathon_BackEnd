package com.bosalpim.compozi_ai.domain.inbox.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class BulkIdsRequestDto {
    @Schema(example = "[1, 2, 3]")
    private List<Long> ids;
}
