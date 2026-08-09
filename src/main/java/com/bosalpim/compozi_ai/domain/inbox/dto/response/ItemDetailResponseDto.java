package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.document.enums.SourceType;
import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import com.bosalpim.compozi_ai.domain.inbox.enums.IssueType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ItemDetailResponseDto {

    private String docId;
    private SourceType sourceType;
    private String supplierName;
    private String rawItemName;
    private String normalizedItemName;
    private String spec;
    private String unit;
    private Long priceBefore;
    private Long priceAfter;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    private ReviewStatus reviewStatus;
    private List<IssueType> exceptionFlags;
    private SourceRef sourceRef;

    private List<ChangeLogDto> changeLog; // 변경 이력 목록

    private Long previousDocId;
    private Long nextDocId;
    private Integer currentIndex;
    private Integer total;

    @Getter
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SourceRef {
        private String inputMethod;
        private String fileName;
        private Long rowNo;

        public static SourceRef of(String inputMethod, String fileName, Long rowNo) {
            return SourceRef.builder()
                    .inputMethod(inputMethod)
                    .fileName(fileName)
                    .rowNo(rowNo)
                    .build();
        }
    }


    public static ItemDetailResponseDto of(
            Item item,
            List<IssueType> exceptionFlags,
            List<ChangeLog> changeLogs,
            Long previousDocId,
            Long nextDocId,
            int currentIndex,
            int total
    ) {
        return ItemDetailResponseDto.builder()
                .docId(item.getDocId())
                .sourceType(item.getSourceType())
                .supplierName(item.getSupplierName())
                .rawItemName(item.getRawItemName())
                .normalizedItemName(item.getNormalizedItemName())
                .spec(item.getSpec())
                .unit(item.getUnit())
                .priceBefore(item.getPriceBefore())
                .priceAfter(item.getPriceAfter())
                .effectiveDate(item.getEffectiveDate())
                .reviewStatus(item.getReviewStatus())
                .exceptionFlags(exceptionFlags)
                .sourceRef(SourceRef.of(
                        item.getFile() != null ? item.getFile().getInputMethod().name().toLowerCase() : null,
                        item.getFile() != null ? item.getFile().getFileName() : null,
                        item.getRowNo()
                ))
                .changeLog(
                        changeLogs == null ? List.of() : changeLogs.stream()
                                .map(ChangeLogDto::of)
                                .toList()
                )
                .previousDocId(previousDocId)
                .nextDocId(nextDocId)
                .currentIndex(currentIndex)
                .total(total)
                .build();
    }
}
