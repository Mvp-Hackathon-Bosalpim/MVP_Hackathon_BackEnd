package com.bosalpim.compozi_ai.domain.document.controller;

import com.bosalpim.compozi_ai.domain.document.component.validator.FileValidator;
import com.bosalpim.compozi_ai.domain.document.dto.request.commonFile.CreateCommonItemDocumentReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.request.manualFile.CreateManualItemDocumentListReqDto;
import com.bosalpim.compozi_ai.domain.document.dto.response.CreateItemDocumentResDto;
import com.bosalpim.compozi_ai.domain.document.dto.response.OcrPreviewResDto;
import com.bosalpim.compozi_ai.domain.document.service.FileService;
import com.bosalpim.compozi_ai.general.response.ApiSuccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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


    // OCR 데이터 조회 결과 확인 API
    @Operation(summary = "OCR 결과 조회", description = "OCR 파일 (이미지, pdf) 입력으로 먼저 파싱 데이터를 조회한다.")
    @ApiSuccess(statusCode = HttpStatus.OK, message = "OCR 파싱이 완료되었습니다.")
    @ApiResponse(responseCode = "400", description = "필수 입력 값이 누락된 경우")
    @PostMapping(value = "/preview/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<OcrPreviewResDto> previewOcrFile(
            @Parameter(
                    description = "OCR 을 활용하여 파싱 조회할 PDF 또는 이미지 (png, jpg ..) 파일 입력",
                    required = true
            )
            @RequestPart("file") MultipartFile file) throws IOException {
        return fileService.previewOcrFile(file);
    }


    // OCR 데이터 전송
    @PostMapping("/confirm/ocr")
    @Operation(summary = "OCR 결과 승인", description = "파싱된 파일을 db 에 저장한다.")
    @ApiSuccess(statusCode = HttpStatus.OK, message = "OCR 파싱이 완료되었습니다.")
    @ApiResponse(responseCode = "400", description = "필수 입력 값이 누락된 경우")
    public CreateItemDocumentResDto confirmOcrItems(
            @RequestBody List<CreateCommonItemDocumentReqDto> reqDtos,
            @RequestParam(value = "filename", required = false) String filename) {

        return fileService.createOcrItem(reqDtos, filename);

    }
}
