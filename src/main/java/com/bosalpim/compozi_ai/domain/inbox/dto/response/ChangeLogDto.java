package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import com.bosalpim.compozi_ai.domain.inbox.entity.ChangeLog;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChangeLogDto {
    private LocalDateTime at;
    private String field;
    private String from;
    private String to;
    private String action;

    public static ChangeLogDto of(ChangeLog changeLog) {
        return ChangeLogDto.builder()
                .at(changeLog.getAt())
                .field(changeLog.getFieldName())
                .from(changeLog.getFromValue())
                .to(changeLog.getToValue())
                .action(String.valueOf(changeLog.getAction()).toLowerCase())
                .build();
    }
}
