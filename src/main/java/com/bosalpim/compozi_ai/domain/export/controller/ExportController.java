package com.bosalpim.compozi_ai.domain.export.controller;

import com.bosalpim.compozi_ai.domain.export.dto.request.ExportRequestDto;
import com.bosalpim.compozi_ai.domain.export.dto.response.DownloadUrlResponseDto;
import com.bosalpim.compozi_ai.domain.export.dto.response.ExportHistoryResponseDto;
import com.bosalpim.compozi_ai.domain.export.entity.ExportHistory;
import com.bosalpim.compozi_ai.domain.export.service.ExportService;
import com.bosalpim.compozi_ai.general.response.ApiSuccess;
import com.bosalpim.compozi_ai.general.response.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Export API", description = "승인 항목 내보내기(JSON/CSV) 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exports")
public class ExportController {

    private final ExportService exportService;

    @Operation(summary = "승인 항목 내보내기 실행", description = "승인(APPROVED) 완료된 품목 전체를 JSON 또는 CSV로 변환하여 S3에 업로드하고 이력을 남긴다.")
    @ApiSuccess(message = "내보내기 요청 성공")
    @ApiResponse(
            responseCode = "400",
            description = "파일명이 없거나 승인된 품목이 없는 경우",
            content = @Content(examples = {
                    @ExampleObject(
                            name = "파일명 누락",
                            value = "{ \"status\": \"FAIL\", \"code\": 400, \"message\": \"파일명을 입력해야 합니다.\", \"data\": null }"
                    ),
                    @ExampleObject(
                            name = "승인 항목 없음",
                            value = "{ \"status\": \"FAIL\", \"code\": 400, \"message\": \"내보낼 승인 완료 항목이 없습니다.\", \"data\": null }"
                    )
            }))
    @PostMapping
    public ExportHistoryResponseDto export(@RequestBody ExportRequestDto request) {
        ExportHistory history = exportService.export(request);
        return ExportHistoryResponseDto.from(history);
    }


    @Operation(summary = "내보내기 파일 다운로드 URL 발급", description = "과거 내보내기 이력의 S3 파일에 대한 임시 다운로드 URL(presigned URL, 10분 유효)을 발급한다.")
    @ApiSuccess(message = "다운로드 URL 발급 성공")
    @ApiResponse(
            responseCode = "404",
            description = "해당 내보내기 이력을 찾을 수 없는 경우",
            content = @Content(examples = @ExampleObject(
                    value = "{ \"status\": \"FAIL\", \"code\": 404, \"message\": \"존재하지 않는 내보내기 이력입니다.\", \"data\": null }"
            )))
    @GetMapping("/{exportHistoryId}/download-url")
    public DownloadUrlResponseDto getDownloadUrl(
            @Parameter(description = "내보내기 이력 ID", required = true)
            @PathVariable Long exportHistoryId) {
        String url = exportService.getDownloadUrl(exportHistoryId);
        return new DownloadUrlResponseDto(url);
    }

    @GetMapping
    @ApiSuccess(message = "내보내기 이력 불러오기 성공.")
    public PageResponseDto<ExportHistoryResponseDto> getExportHistory(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {

        return exportService.getHistories(page, size);
    }


}
