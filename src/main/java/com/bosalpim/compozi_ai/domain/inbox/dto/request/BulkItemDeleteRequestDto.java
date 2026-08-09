package com.bosalpim.compozi_ai.domain.inbox.dto.request;


import java.util.List;
import lombok.Getter;

@Getter
public class BulkItemDeleteRequestDto {

    private List<Long> ids;
}
