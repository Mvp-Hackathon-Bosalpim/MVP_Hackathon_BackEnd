package com.bosalpim.compozi_ai.general.advice;

import com.bosalpim.compozi_ai.general.exception.CustomException;
import com.bosalpim.compozi_ai.general.response.ApiResponse;
import com.bosalpim.compozi_ai.general.util.StatusCodeHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> handleCustomException(CustomException e) {
        StatusCodeHelper statusCodeHelper = StatusCodeHelper.of(e.getBadStatusCode());
        log.error("🚨 CustomException 발생: ", e);
        return ResponseEntity
                .status(statusCodeHelper.getHttpStatus())
                .body(ApiResponse.fail(statusCodeHelper.getStatusValue(), statusCodeHelper.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("🚨 DataIntegrityViolationException 발생: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "중복된 데이터가 존재하거나 참조 관계가 올바르지 않습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleAllException(Exception e) {
        log.error("🚨 Exception 발생: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
    }
}
