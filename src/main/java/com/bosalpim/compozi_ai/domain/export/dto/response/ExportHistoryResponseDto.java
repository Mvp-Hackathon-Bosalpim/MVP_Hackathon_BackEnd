package com.bosalpim.compozi_ai.domain.export.dto.response;

import com.bosalpim.compozi_ai.domain.export.entity.ExportHistory;
import com.bosalpim.compozi_ai.domain.export.enums.ExportFormat;
import com.bosalpim.compozi_ai.domain.export.enums.ExportStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportHistoryResponseDto {
    private Long id;
    private String fileName;
    private ExportFormat format;
    private Integer exportedCount;
    private LocalDateTime exportedAt;
    private ExportStatus status;

    public static ExportHistoryResponseDto from(ExportHistory history) {
        return new ExportHistoryResponseDto(
                history.getId(),
                history.getFileName(),
                history.getFormat(),
                history.getExportedCount(),
                history.getExportedAt(),
                history.getStatus()
        );
    }
}
