package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChangeLogDto {
    private String field;
    private String beforeValue;
    private String afterValue;

    public static ChangeLogDto of(ChangeLog changeLog) {
        return ChangeLogDto.builder()
                .field(changeLog.getFieldName())
                .beforeValue(changeLog.getFromValue())
                .afterValue(changeLog.getToValue())
                .build();
    }
}
