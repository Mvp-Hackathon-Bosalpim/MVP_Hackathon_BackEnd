package com.bosalpim.compozi_ai.domain.document.dto.response;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateItemDocumentResDto {


    private final Integer total;
    private final Integer normal;

    @JsonProperty("need_checked")
    private final Integer needCheck;


    public static CreateItemDocumentResDto from(List<Item> items) {
        int needChecked = (int) items.stream()
                .filter(item -> item.getReviewStatus() == ReviewStatus.ON_HOLD
                        || item.getReviewStatus() == ReviewStatus.NEEDS_REVIEW)
                .count();

        return CreateItemDocumentResDto.builder()
                .total(items.size())
                .normal(items.size() - needChecked)
                .needCheck(needChecked)
                .build();

    }
}
