package com.bosalpim.compozi_ai.domain.dashboard.dto.response;

import com.bosalpim.compozi_ai.domain.export.entity.ExportHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "대시보드 요약 응답")
public class DashboardSummaryResponseDto {

    @Schema(description = "전체 누적 기준 카운트")
    private final CountSet total;

    @Schema(description = "오늘 기준 카운트")
    private final CountSet today;

    @Schema(description = "최근 데이터 내보내기 이력 (최대 2건)")
    private final List<RecentExportDto> recentExports;

    @Data
    @AllArgsConstructor
    @Schema(description = "상태별 카운트 묶음 (막대 그래프)")
    public static class CountSet {
        @Schema(description = "전체 품목 건수", example = "42")
        private final long totalItems;

        @Schema(description = "승인완료 건수", example = "15")
        private final long approved;

        @Schema(description = "예외탐지(미해결 이슈 보유) 건수", example = "8")
        private final long exceptionDetected;

        @Schema(description = "접수대기 건수", example = "5")
        private final long pending;
    }

    @Data
    @AllArgsConstructor
    @Schema(description = "최근 내보내기 이력 항목")
    public static class RecentExportDto {
        @Schema(description = "내보내기 이력 ID", example = "123")
        private final Long id;

        @Schema(description = "파일명", example = "qwertyuiopasdf.json")
        private final String fileName;

        @Schema(description = "내보내기 형식", example = "JSON")
        private final String format;

        @Schema(description = "내보내기 일시", example = "2026-08-11T14:36:00")
        private final String exportedAt;

        @Schema(description = "생성 건수", example = "1234")
        private final int exportedCount;

        @Schema(description = "상태", example = "COMPLETED")
        private final String status;

        public static RecentExportDto from(ExportHistory history) {
            return new RecentExportDto(
                    history.getId(),
                    history.getFileName(),
                    history.getFormat().name(),
                    history.getExportedAt().toString(),
                    history.getExportedCount(),
                    history.getStatus().name()
            );
        }
    }
}