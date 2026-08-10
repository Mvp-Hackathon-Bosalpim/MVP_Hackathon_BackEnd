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
    ALL_ITEMS_FAILED(HttpStatus.BAD_REQUEST, "요청한 모든 품목의 처리에 실패했습니다."),
    INVALID_STATUS_FOR_RE_REVIEW(HttpStatus.BAD_REQUEST, "승인 또는 반려된 항목만 재검토할 수 있습니다."),
    EXPORT_NO_APPROVED_ITEMS(HttpStatus.BAD_REQUEST, "내보낼 승인 완료 항목이 없습니다."),
    EXPORT_FILE_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "파일명을 입력해야 합니다."),
    EXPORT_S3_KEY_NOT_FOUND(HttpStatus.BAD_REQUEST, "다운로드할 파일이 존재하지 않습니다. 내보내기가 실패한 이력입니다."),
    APPROVAL_MEMO_REQUIRED(HttpStatus.BAD_REQUEST, "예외가 남아있는 상태로 승인하려면 승인 사유(메모)를 입력해야 합니다."),
    NO_ITEM_CONTENT(HttpStatus.BAD_REQUEST, "조건에 맞는 item 이 없습니다."),
    ITEM_CANNOT_REMOVE(HttpStatus.BAD_REQUEST, "이미 처리 (승인, 반려, 삭제) 되거나 없는 값이 있어 삭제 할 수 없습니다."),

    //401 UNAUTHORIZED

    //403 FORBIDDEN

    //404 NOT FOUND
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 문서를 찾을 수 없습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 품목을 찾을 수 없습니다."),
    ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이슈를 찾을 수 없습니다."),
    EXPORT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 내보내기 이력입니다."),

    //5xx SERVER ERROR
    FILE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    EXPORT_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
