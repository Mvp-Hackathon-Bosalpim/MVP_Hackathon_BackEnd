package com.bosalpim.compozi_ai.domain.export.dto.request;

import com.bosalpim.compozi_ai.domain.export.enums.ExportFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportRequestDto {
    private ExportFormat format;
    private String fileName;
}
