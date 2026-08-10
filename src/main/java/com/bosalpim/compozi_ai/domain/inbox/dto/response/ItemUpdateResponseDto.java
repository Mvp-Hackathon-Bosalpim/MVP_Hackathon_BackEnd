package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemUpdateResponseDto {

    private Long id;

    @JsonProperty("last_modified_at")
    private LocalDateTime lastModifiedAt;

    public static ItemUpdateResponseDto update(Item item) {
        return ItemUpdateResponseDto.builder()
                .id(item.getId())
                .lastModifiedAt(LocalDateTime.now())
                .build();
    }
}
