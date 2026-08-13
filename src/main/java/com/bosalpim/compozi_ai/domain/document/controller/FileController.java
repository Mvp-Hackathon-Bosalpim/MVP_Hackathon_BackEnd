package com.bosalpim.compozi_ai.domain.document.controller;

import com.bosalpim.compozi_ai.domain.document.component.validator.FileValidator;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentListReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.response.CreateItemDocumentResDto;
import com.bosalpim.compozi_ai.domain.document.service.FileService;
import com.bosalpim.compozi_ai.general.response.ApiSuccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "File API", description = "구매 파일 (수기) 정보 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FileController {

    private final FileService fileService;
    private final FileValidator fileValidator;

    @Operation(summary = "파일 업로드", description = "파일 입력으로 구매 데이터를 입력한다.")
    @ApiSuccess(statusCode = HttpStatus.CREATED, message = "파일 입력이 완료 되었습니다.")
    @ApiResponse(responseCode = "400", description = "잘못된 파일 확장자이거나 빈 파일인 경우")
    @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CreateItemDocumentResDto createCommonFile(
            @Parameter(
                    description = "업로드할 엑셀/csv 등 구매 증빙 파일",
                    required = true
            )
            @RequestPart("file") MultipartFile file) throws IOException {
        fileValidator.validateDocumentFile(file);
        return fileService.createCommonFile(file);
    }


    @Operation(summary = "수기 업로드", description = "수기 입력으로 구매 데이터를 입력한다.")
    @ApiSuccess(statusCode = HttpStatus.CREATED, message = "수기 입력이 완료 되었습니다.")
    @ApiResponse(responseCode = "400", description = "필수 입력 값이 누락된 경우")
    @PostMapping("/manual-document")
    public CreateItemDocumentResDto createManualFile(@Valid @RequestBody CreateManualItemDocumentListReqDto reqDto) {
        return fileService.createManualFile(reqDto);
    }
}
