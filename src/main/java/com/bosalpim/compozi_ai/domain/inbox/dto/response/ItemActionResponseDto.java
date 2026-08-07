package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemActionResponseDto {
    @Schema(description = "처리된 품목 ID", example = "1")
    private Long id;
}
