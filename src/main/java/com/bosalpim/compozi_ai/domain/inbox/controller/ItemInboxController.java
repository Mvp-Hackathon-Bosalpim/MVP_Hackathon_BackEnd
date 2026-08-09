package com.bosalpim.compozi_ai.domain.inbox.controller;

import com.bosalpim.compozi_ai.domain.document.enums.ReviewStatus;
import com.bosalpim.compozi_ai.domain.inbox.dto.request.BulkIdsRequestDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.request.ItemSearchRequestDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.request.ItemUpdateRequestDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.request.MemoRequestDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.BulkActionResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.ItemActionResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.ItemDetailResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.ItemListResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.dto.response.StatusCountResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.service.InboxService;
import com.bosalpim.compozi_ai.general.response.ApiSuccess;
import com.bosalpim.compozi_ai.general.response.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inbox API", description = "품목 검수(승인/반려) 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ItemInboxController {

    private final InboxService inboxService;

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
            @PathVariable long id,
            @RequestBody(required = false) MemoRequestDto reqDto) {
        String memo = (reqDto != null) ? reqDto.getMemo() : null;
        Long approveId = inboxService.approve(id, memo);
        return new ItemActionResponseDto(approveId);
    }

    @Operation(summary = "특정 품목 반려", description = "품목을 반려 처리하고 반려 사유를 기록한다.")
    @ApiSuccess(message = "반려 요청 성공")
    @ApiResponse(
            responseCode = "404",
            description = "해당 품목을 찾을 수 없는 경우",
            content = @Content(examples = @ExampleObject(
                    value = "{ \"status\": \"FAIL\", \"code\": 404, \"message\": \"해당 품목을 찾을 수 없습니다.\", \"data\": null }"
            )))
    @ApiResponse(
            responseCode = "400",
            description = "이미 승인되었거나 이미 반려된 품목인 경우",
            content = @Content(examples = {
                    @ExampleObject(
                            name = "이미 승인됨",
                            value = "{ \"status\": \"FAIL\", \"code\": 400, \"message\": \"이미 승인된 항목은 수정할 수 없습니다.\", \"data\": null }"
                    ),
                    @ExampleObject(
                            name = "이미 반려됨",
                            value = "{ \"status\": \"FAIL\", \"code\": 400, \"message\": \"이미 반려된 항목입니다.\", \"data\": null }"
                    )
            }))
    @PostMapping("/documents/{id}/reject")
    public ItemActionResponseDto reject(
            @Parameter(description = "품목 ID", required = true)
            @PathVariable long id,
            @RequestBody(required = false) MemoRequestDto reqDto) {
        String memo = (reqDto != null) ? reqDto.getMemo() : null;
        Long rejectId = inboxService.reject(id, memo);
        return new ItemActionResponseDto(rejectId);

    }

    @Operation(
            summary = "여러 품목 일괄 승인",
            description = "요청받은 id 목록 중 승인 가능한 품목만 승인 처리하고, 실패한 항목은 사유와 함께 반환한다."
    )
    @ApiSuccess(message = "요청한 품목이 모두 성공적으로 승인되었습니다.")
    @ApiResponse(
            responseCode = "400",
            description = "요청한 모든 품목의 처리에 실패한 경우",
            content = @Content(examples = @ExampleObject(
                    value = "{ \"status\": \"FAIL\", \"code\": 400, \"message\": \"요청한 모든 품목의 처리에 실패했습니다.\", \"data\": null }"
            )))
    @PostMapping("/documents/bulk-approve")
    public BulkActionResponseDto bulkApprove(@RequestBody BulkIdsRequestDto reqDto) {
        return inboxService.bulkApprove(reqDto.getIds(), reqDto.getMemo());
    }

    @Operation(summary = "여러 품목 일괄 반려", description = "요청받은 id 목록 중 반려 가능한 품목만 반려 처리하고, 실패한 항목은 사유와 함께 반환한다.")
    @ApiSuccess(message = "요청한 품목이 모두 성공적으로 반려되었습니다.")
    @ApiResponse(
            responseCode = "400",
            description = "요청한 모든 품목의 처리에 실패한 경우",
            content = @Content(examples = @ExampleObject(
                    value = "{ \"status\": \"FAIL\", \"code\": 400, \"message\": \"요청한 모든 품목의 처리에 실패했습니다.\", \"data\": null }"
            )))
    @PostMapping("/documents/bulk-reject")
    public BulkActionResponseDto bulkReject(@RequestBody BulkIdsRequestDto reqDto) {
        return inboxService.bulkReject(reqDto.getIds(), reqDto.getMemo());
    }

    @Operation(summary = "여러 품목 일괄 재검토", description = "승인 또는 반려된 품목들을 다시 검토 대기(새 항목) 상태로 되돌린다.")
    @ApiSuccess(message = "요청한 품목이 모두 성공적으로 재검토 처리되었습니다.")
    @ApiResponse(
            responseCode = "400",
            description = "요청한 모든 품목의 처리에 실패한 경우",
            content = @Content(examples = @ExampleObject(
                    value = "{ \"status\": \"FAIL\", \"code\": 400, \"message\": \"요청한 모든 품목의 처리에 실패했습니다.\", \"data\": null }"
            )))
    @PostMapping("/documents/bulk-re-review")
    public BulkActionResponseDto bulkReReview(@RequestBody BulkIdsRequestDto reqDto) {
        return inboxService.bulkReReview(reqDto.getIds(), reqDto.getMemo());
    }

    @Operation(summary = "전체 품목 목록 조회", description = "삭제되지 않은 품목 전체를 페이지네이션하여 조회한다.")
    @ApiSuccess(message = "전체 조회 성공")
    @GetMapping("/documents")
    public PageResponseDto<ItemListResponseDto> getItems(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return inboxService.getItems(pageable);
    }

    @Operation(summary = "품목 상태별 개수 조회", description = "review_status별 전체 품목 개수를 조회한다.")
    @ApiSuccess(message = "상태별 개수 조회 성공")
    @GetMapping("/documents/status-counts")
    public StatusCountResponseDto getStatusCounts() {
        return inboxService.getStatusCounts();
    }

    @Operation(summary = "품목명 목록 조회", description = "필터에 사용할 정규화 품목명 목록을 중복 없이 가나다순으로 조회한다.")
    @ApiSuccess(message = "품목명 목록 조회 성공")
    @GetMapping("/items/normalized-item-names")
    public List<String> getNormalizedItemNames() {
        return inboxService.getNormalizedItemNames();
    }

    @Operation(summary = "공급사명 목록 조회", description = "필터에 사용할 공급사명 목록을 중복 없이 가나다순으로 조회한다.")
    @ApiSuccess(message = "공급사명 목록 조회 성공")
    @GetMapping("/items/supplier-names")
    public List<String> getSupplierNames() {
        return inboxService.getSupplierNames();
    }


    @Operation(summary = "품목 필터 검색", description = "품목명/공급사명/적용일 범위/상태로 품목을 검색한다. 품목명·공급사명은 다중 선택, 상태는 단일 선택 가능하다.")
    @ApiSuccess(message = "필터 검색 성공")
    @PostMapping("/documents/search")
    public PageResponseDto<ItemListResponseDto> searchItems(
            @RequestBody(required = false) ItemSearchRequestDto searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<String> itemNames = searchRequest != null ? searchRequest.getItemNames() : null;
        List<String> supplierNames = searchRequest != null ? searchRequest.getSupplierNames() : null;
        LocalDate startDate = searchRequest != null ? searchRequest.getStartDate() : null;
        LocalDate endDate = searchRequest != null ? searchRequest.getEndDate() : null;
        ReviewStatus reviewStatus = searchRequest != null ? searchRequest.getReviewStatus() : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return inboxService.searchItems(itemNames, supplierNames, startDate, endDate, reviewStatus, pageable);
    }

    @Operation(summary = "품목 단건 조회", description = "품목 (item) id 를 기반으로 구매 품목을 단건 조회한다.")
    @ApiSuccess(message = "특정 구매 품목 조회 성공")
    @GetMapping("/documents/{id}")
    public ItemDetailResponseDto searchDetailItem(
            @Parameter(description = "품목 ID", required = true)
            @PathVariable Long id
    ) {
        return inboxService.getDetailItem(id);
    }

    @Operation(summary = "품목 단건 수정", description = "품목 (item) id 를 기반으로 특정 품목을 수정요청 한다.")
    @ApiSuccess(message = "특정 구매 품목 수정 성공")
    @PatchMapping("/documents/{id}")
    public Void updateDetailItem(
            @Parameter(description = "품목 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "수정 내용", required = true)
            @RequestBody ItemUpdateRequestDto reqDto
    ) {

        return inboxService.updateDetailItem(id, reqDto);

    }

    @Operation(summary = "품목 단건 삭제", description = "품목 (item) id 를 기반으로 특정 품목을 삭제요청 한다.")
    @ApiSuccess(message = "특정 구매 품목 삭제 성공")
    @DeleteMapping("/documents/{id}")
    public Void deleteDetailItem(
            @Parameter(description = "품목 ID", required = true)
            @PathVariable Long id
    ) {
        return inboxService.deleteDetailItem(id);
    }

}
