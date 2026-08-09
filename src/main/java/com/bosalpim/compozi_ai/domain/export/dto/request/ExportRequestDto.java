package com.bosalpim.compozi_ai.domain.export.dto.request;

import com.bosalpim.compozi_ai.domain.export.enums.ExportFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExportRequestDto {
    private ExportFormat format;
    private String fileName;
}
