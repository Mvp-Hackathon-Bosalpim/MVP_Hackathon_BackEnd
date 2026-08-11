package com.bosalpim.compozi_ai.domain.document.dto.request.commonFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class CreateCommonItemDocumentReqDto {

    @NotBlank(message = "docId 가 누락되었습니다.")
    private String docId;

    @NotBlank(message = "sourceType 이 누락되었습니다.")
    private String sourceType;

    private Long rowNo;

    @NotBlank(message = "공급사명이 누락되었습니다.")
    private String supplierName;

    @NotBlank(message = "원본 품목명이 누락되었습니다.")
    private String rawItemName;

    @Setter
    private String normalizedItemName;

    @NotBlank(message = "규격이 누락되었습니다.")
    private String spec;

    @NotBlank(message = "단위가 누락되었습니다.")
    private String unit;

    @NotNull(message = "변경 전 단가가 누락되었습니다.")
    private Long priceBefore;

    @NotNull(message = "변경 후 단가가 누락되었습니다.")
    private Long priceAfter;

    @NotNull(message = "적용일이 누락되었습니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    @Setter
    private String duplicateGroupKey;

    private boolean hasParseError;


}
