package com.bosalpim.compozi_ai.domain.inbox.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemDeleteRequestDto {
    private String memo;
}
