package com.bosalpim.compozi_ai.domain.document.dto.request.commonFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommonItemDocumentReqDto {

    @NotBlank(message = "docId 가 누락되었습니다.")
    @JsonProperty("doc_id")
    private String docId;

    @NotBlank(message = "sourceType 이 누락되었습니다.")
    @JsonProperty("source_type")
    private String sourceType;

    @JsonProperty("row_no")
    private Long rowNo;

    @NotBlank(message = "공급사명이 누락되었습니다.")
    @JsonProperty("supplier_name")
    private String supplierName;

    @NotBlank(message = "원본 품목명이 누락되었습니다.")
    @JsonProperty("raw_item_name")
    private String rawItemName;

    @Setter
    @JsonProperty("normalized_item_name")
    private String normalizedItemName;

    @NotBlank(message = "규격이 누락되었습니다.")
    @JsonProperty("spec")
    private String spec;

    @NotBlank(message = "단위가 누락되었습니다.")
    @JsonProperty("unit")
    private String unit;

    @NotNull(message = "변경 전 단가가 누락되었습니다.")
    @JsonProperty("price_before")
    private Long priceBefore;

    @NotNull(message = "변경 후 단가가 누락되었습니다.")
    @JsonProperty("price_after")
    private Long priceAfter;

    @NotNull(message = "적용일이 누락되었습니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @Setter
    @JsonProperty("duplicate_group_key")
    private String duplicateGroupKey;

    @JsonProperty("has_parse_error")
    private boolean hasParseError;

}
