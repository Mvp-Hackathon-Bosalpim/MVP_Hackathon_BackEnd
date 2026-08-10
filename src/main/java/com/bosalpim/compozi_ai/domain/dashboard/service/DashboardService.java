package com.bosalpim.compozi_ai.domain.dashboard.service;

import com.bosalpim.compozi_ai.domain.dashboard.dto.IssueSummaryDto;
import com.bosalpim.compozi_ai.domain.dashboard.dto.StatusCountDto;
import com.bosalpim.compozi_ai.domain.dashboard.dto.response.DashboardSummaryResponseDto;
import com.bosalpim.compozi_ai.domain.dashboard.dto.response.DashboardSummaryResponseDto.CountSet;
import com.bosalpim.compozi_ai.domain.dashboard.dto.response.DashboardSummaryResponseDto.RecentExportDto;
import com.bosalpim.compozi_ai.domain.dashboard.dto.response.IssueStatResponseDto;
import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.document.repository.item.ItemRepository;
import com.bosalpim.compozi_ai.domain.export.entity.ExportHistory;
import com.bosalpim.compozi_ai.domain.export.repository.ExportHistoryRepository;
import com.bosalpim.compozi_ai.domain.inbox.enums.Action;
import com.bosalpim.compozi_ai.domain.inbox.repository.change_log.ChangeLogRepository;
import com.bosalpim.compozi_ai.domain.inbox.repository.issue.IssueRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ItemRepository itemRepository;
    private final IssueRepository issueRepository;
    private final ChangeLogRepository changeLogRepository;
    private final ExportHistoryRepository exportHistoryRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponseDto getSummary() {
        LocalDateTime todayStart = LocalDate.now(KST).atStartOfDay();

        List<StatusCountDto> statusCounts = itemRepository.countByReviewStatusForDashboard(todayStart);
        IssueSummaryDto issueSummary = issueRepository.countUnresolvedItemsForDashboard(todayStart);
        long todayApproved = changeLogRepository.countByActionAndAtGreaterThanEqual(Action.APPROVE, todayStart);
        List<ExportHistory> recentExports = exportHistoryRepository.findTop2ByOrderByExportedAtDesc();

        CountSet total = buildCountSet(statusCounts, issueSummary, StatusCountDto::getTotalCount,
                IssueSummaryDto::getTotalCount);
        CountSet today = buildCountSet(statusCounts, issueSummary, StatusCountDto::getTodayCount,
                IssueSummaryDto::getTodayCount);

        today = new CountSet(today.getTotalItems(), todayApproved, today.getExceptionDetected(), today.getPending());

        List<RecentExportDto> recentExportDtos = recentExports.stream()
                .map(RecentExportDto::from)
                .toList();

        return new DashboardSummaryResponseDto(total, today, recentExportDtos);
    }

    private CountSet buildCountSet(
            List<StatusCountDto> statusCounts,
            IssueSummaryDto issueSummary,
            java.util.function.Function<StatusCountDto, Long> countExtractor,
            java.util.function.Function<IssueSummaryDto, Long> issueCountExtractor
    ) {
        long totalItems = statusCounts.stream()
                .mapToLong(dto -> countExtractor.apply(dto))
                .sum();

        long approved = statusCounts.stream()
                .filter(dto -> dto.getReviewStatus() == ReviewStatus.APPROVED)
                .mapToLong(dto -> countExtractor.apply(dto))
                .findFirst()
                .orElse(0L);

        long pending = statusCounts.stream()
                .filter(dto -> dto.getReviewStatus() == ReviewStatus.NEW)
                .mapToLong(dto -> countExtractor.apply(dto))
                .findFirst()
                .orElse(0L);

        long exceptionDetected = issueCountExtractor.apply(issueSummary);

        return new CountSet(totalItems, approved, exceptionDetected, pending);
    }

    @Transactional(readOnly = true)
    public IssueStatResponseDto getIssueStats() {
        return new IssueStatResponseDto(issueRepository.countByIssueType());
    }
}