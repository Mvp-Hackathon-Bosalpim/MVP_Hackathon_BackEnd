package com.bosalpim.compozi_ai.domain.dashboard.controller;

import com.bosalpim.compozi_ai.domain.dashboard.dto.response.DashboardSummaryResponseDto;
import com.bosalpim.compozi_ai.domain.dashboard.dto.response.IssueStatResponseDto;
import com.bosalpim.compozi_ai.domain.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "대시보드 요약 정보 조회 API")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "대시보드 요약 조회",
            description = "전체/오늘 기준 품목 상태 카운트(전체 건수, 승인완료, 예외탐지, 접수대기)와 "
                    + "최근 데이터 내보내기 이력 2건을 조회합니다."
    )
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponseDto> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @Operation(
            summary = "이슈 유형별 통계 조회",
            description = "미해결 이슈를 issue_type별로 그룹핑하여 건수를 조회합니다. "
                    + "우선 검수가 필요한 항목을 유형별로 파악하는 데 사용됩니다."
    )
    @GetMapping("/issue-stats")
    public ResponseEntity<IssueStatResponseDto> getIssueStats() {
        return ResponseEntity.ok(dashboardService.getIssueStats());
    }
}