package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import com.bosalpim.compozi_ai.domain.document.entity.Item;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemDeleteResponseDto {

    private Long id;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    public static ItemDeleteResponseDto delete(Item item) {
        return ItemDeleteResponseDto.builder()
                .id(item.getId())
                .deletedAt(item.getDeletedAt())
                .build();
    }
}
