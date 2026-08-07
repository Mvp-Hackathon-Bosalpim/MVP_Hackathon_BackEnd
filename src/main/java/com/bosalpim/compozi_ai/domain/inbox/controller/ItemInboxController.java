package com.bosalpim.compozi_ai.domain.inbox.controller;

import com.bosalpim.compozi_ai.domain.inbox.dto.ItemActionResponseDto;
import com.bosalpim.compozi_ai.domain.inbox.service.IssueService;
import com.bosalpim.compozi_ai.general.response.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ItemInboxController {
    private final IssueService issueService;

    @ApiSuccess(message = "승인 요청 성공")
    @PostMapping("/documents/{id}/approve")
    public ItemActionResponseDto approve(@PathVariable long id) {
        Long approveId = issueService.approve(id);
        return new ItemActionResponseDto(approveId);
    }
}
