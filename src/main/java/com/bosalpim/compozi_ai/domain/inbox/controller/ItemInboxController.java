package com.bosalpim.compozi_ai.domain.inbox.controller;

import com.bosalpim.compozi_ai.domain.inbox.dto.ItemActionResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.service.IssueService;
import com.bosalpim.compozi_ai.general.response.ApiSuccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inbox API", description = "품목 검수(승인/반려) 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ItemInboxController {

    private final IssueService issueService;

    @Operation(summary = "특정 품목 승인", description = "미해결 이슈가 없는 품목을 승인 처리한다.")
    @ApiSuccess(message = "승인 요청 성공")
    @ApiResponse(
            responseCode = "404",
            description = "해당 품목을 찾을 수 없는 경우",
            content = @Content(examples = @ExampleObject(
                    value = "{ \"status\": \"FAIL\", \"code\": 404, \"message\": \"해당 품목을 찾을 수 없습니다.\", \"data\": null }"
            )))
    @ApiResponse(
            responseCode = "400",
            description = "이미 승인된 품목이거나 미해결 이슈가 존재하는 경우",
            content = @Content(examples = @ExampleObject(
                    value = "{ \"status\": \"FAIL\", \"code\": 400, \"message\": \"해결되지 않은 검증 이슈가 있어 승인할 수 없습니다.\", \"data\": null }"
            )))
    @PostMapping("/documents/{id}/approve")
    public ItemActionResponseDto approve(
            @Parameter(description = "품목 ID", required = true)
            @PathVariable long id) {
        Long approveId = issueService.approve(id);
        return new ItemActionResponseDto(approveId);
    }
}