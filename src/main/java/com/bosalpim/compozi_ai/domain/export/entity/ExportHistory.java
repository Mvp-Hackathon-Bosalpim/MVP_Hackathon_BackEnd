package com.bosalpim.compozi_ai.domain.export.entity;

import com.bosalpim.compozi_ai.domain.export.enums.ExportFormat;
import com.bosalpim.compozi_ai.domain.export.enums.ExportStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "export_histories")
public class ExportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String s3Key;

    @Enumerated(EnumType.STRING)
    private ExportFormat format;

    private Integer exportedCount;

    private LocalDateTime exportedAt;

    @Enumerated(EnumType.STRING)
    private ExportStatus status;

    @Builder
    public ExportHistory(String fileName, String s3Key, ExportFormat format,
                         Integer exportedCount, LocalDateTime exportedAt, ExportStatus status) {
        this.fileName = fileName;
        this.s3Key = s3Key;
        this.format = format;
        this.exportedCount = exportedCount;
        this.exportedAt = exportedAt;
        this.status = status;
    }

    public static ExportHistory completed(String fileName, String s3Key, ExportFormat format, int count) {
        return ExportHistory.builder()
                .fileName(fileName)
                .s3Key(s3Key)
                .format(format)
                .exportedCount(count)
                .exportedAt(LocalDateTime.now())
                .status(ExportStatus.COMPLETED)
                .build();
    }

    public static ExportHistory failed(String fileName, ExportFormat format) {
        return ExportHistory.builder()
                .fileName(fileName)
                .s3Key(null)
                .format(format)
                .exportedCount(0)
                .exportedAt(LocalDateTime.now())
                .status(ExportStatus.FAILED)
                .build();
    }
}
