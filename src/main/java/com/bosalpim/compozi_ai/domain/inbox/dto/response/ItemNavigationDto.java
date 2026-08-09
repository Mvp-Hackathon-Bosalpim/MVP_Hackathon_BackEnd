package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemNavigationDto {
    private Long targetId;
    private Long targetIndex;
    private Long prevId;
    private Long nextId;
    private Long totalCount;
}
