package com.bosalpim.compozi_ai.domain.export.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ExportItemCsvResponseDto {
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
    private String exceptionFlags;
    private String sourceInputMethod;
    private String sourceFileName;
    private Long sourceRowNo;
    private LocalDateTime reviewedAt;
    private String reviewMemo;
    private String changeLogAt;
    private String changeLogField;
    private String changeLogFrom;
    private String changeLogTo;
    private String changeLogAction;
}