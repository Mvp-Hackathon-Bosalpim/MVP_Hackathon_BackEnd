package com.bosalpim.compozi_ai.domain.inbox.dto.response;

import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BulkActionResponseDto {
    private int requestedCount;
    private int successCount;
    private int failedCount;
    private List<Long> successIds;
    private List<FailedItemDto> failedList;

    @Data
    public static class FailedItemDto {
        private Long id;
        private String reason;
        private List<String> issueTypes;

        public FailedItemDto(Long id, BadStatusCode badStatusCode) {
            this.id = id;
            this.reason = badStatusCode.getMessage();
        }

        public FailedItemDto(Long id, BadStatusCode badStatusCode, List<String> issueTypes) {
            this(id, badStatusCode);
            this.issueTypes = issueTypes;
        }
    }
}