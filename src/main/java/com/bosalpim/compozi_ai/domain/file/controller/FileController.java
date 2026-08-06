package com.bosalpim.compozi_ai.domain.file.controller;

import com.bosalpim.compozi_ai.domain.file.dto.request.manualFile.CreateManualItemDocumentListReqDto;
import com.bosalpim.compozi_ai.domain.file.dto.response.manualFile.CreateManualItemDocumentResDto;
import com.bosalpim.compozi_ai.domain.file.service.FileService;
import com.bosalpim.compozi_ai.general.response.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FileController {

    private final FileService fileService;


    @ApiSuccess(statusCode = HttpStatus.CREATED, message = "수기 입력이 완료 되었습니다.")
    @PostMapping("/manual-document")
    public CreateManualItemDocumentResDto createManualFile(@RequestBody CreateManualItemDocumentListReqDto reqDto) {
        return fileService.createManualFile(reqDto);
    }
}
