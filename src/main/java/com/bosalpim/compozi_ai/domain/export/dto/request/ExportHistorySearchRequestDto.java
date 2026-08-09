package com.bosalpim.compozi_ai.domain.export.dto.request;

import com.bosalpim.compozi_ai.domain.export.enums.ExportFormat;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportHistorySearchRequestDto {
    private String fileName;
    private ExportFormat format;
    private LocalDate startDate;
    private LocalDate endDate;
}
