package com.bosalpim.compozi_ai.domain.export.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportItemJsonResponseDto {

    private String docId;
    private String sourceType;
    private String supplierName;
    private String rawItemName;
    private String normalizedItemName;
    private String spec;
    private String unit;
    private Long priceBefore;
    private Long priceAfter;
    private LocalDate effectiveDate;
    private String reviewStatus;
    private List<String> exceptionFlags;
    private SourceRef sourceRef;
    private LocalDateTime reviewedAt;
    private String reviewMemo;
    private List<ChangeLogDto> changeLog;

    @Data
    @AllArgsConstructor
    public static class SourceRef {
        private String inputMethod;
        private String fileName;
        private Long rowNo;
    }

    @Data
    @AllArgsConstructor
    public static class ChangeLogDto {
        private LocalDateTime at;
        private String field;
        private String from;
        private String to;
        private String action;
    }
}
