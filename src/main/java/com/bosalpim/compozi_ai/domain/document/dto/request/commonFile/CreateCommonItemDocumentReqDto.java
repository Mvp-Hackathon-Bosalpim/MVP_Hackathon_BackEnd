package com.bosalpim.compozi_ai.domain.document.dto.request.commonFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class CreateCommonItemDocumentReqDto {

    private String docId;

    private String sourceType;

    private Long rowNo;

    private String supplierName;

    private String rawItemName;

    @Setter
    private String normalizedItemName;

    private String spec;

    private String unit;

    private Long priceBefore;

    private Long priceAfter;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    @Setter
    private String duplicateGroupKey;

    private boolean hasParseError;


}
