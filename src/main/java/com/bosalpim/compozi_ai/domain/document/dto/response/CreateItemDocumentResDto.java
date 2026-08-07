package com.bosalpim.compozi_ai.domain.document.dto.response;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
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
        return CreateItemDocumentResDto.builder()
                .total(items.size())
                .normal(1)
                .needCheck(items.size() - 1)
                .build();
        // TODO : 추후 데이터 유효성 검사 로직 구현 시 수정! (일단 하드코딩)

    }
}
