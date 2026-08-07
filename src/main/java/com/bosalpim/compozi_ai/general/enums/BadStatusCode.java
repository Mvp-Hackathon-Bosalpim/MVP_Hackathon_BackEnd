package com.bosalpim.compozi_ai.general.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BadStatusCode {
    //400 BAD REQUEST
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원되지 않는 파일 형식입니다. XLSX 또는 CSV 파일만 업로드 가능합니다."),
    FILE_PARSE_FAILED(HttpStatus.BAD_REQUEST, "파일을 파싱하는 데 실패했습니다."),
    DUPLICATE_DOC_ID_IN_FILE(HttpStatus.BAD_REQUEST, "같은 파일 안에 동일한 문서ID가 이미 존재합니다."),
    UNRESOLVED_ISSUE_EXISTS(HttpStatus.BAD_REQUEST, "해결되지 않은 검증 이슈가 있어 승인할 수 없습니다."),
    REJECT_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "반려 사유를 입력해야 합니다."),
    ITEM_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제 처리된 항목입니다."),
    ITEM_ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "이미 승인된 항목은 수정할 수 없습니다."),
    ITEM_LOADING_FAIL(HttpStatus.BAD_REQUEST, "해당 사전 데이터 로딩을 실패 했습니다."),
    ITEM_ALREADY_REJECTED(HttpStatus.BAD_REQUEST, "이미 반려된 항목입니다."),

    //401 UNAUTHORIZED

    //403 FORBIDDEN

    //404 NOT FOUND
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 문서를 찾을 수 없습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 품목을 찾을 수 없습니다."),
    ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이슈를 찾을 수 없습니다."),

    //5xx SERVER ERROR
    FILE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
