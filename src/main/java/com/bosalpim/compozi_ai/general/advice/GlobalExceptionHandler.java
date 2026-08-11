package com.bosalpim.compozi_ai.general.advice;

import com.bosalpim.compozi_ai.general.exception.CustomException;
import com.bosalpim.compozi_ai.general.response.ApiResponse;
import com.bosalpim.compozi_ai.general.util.StatusCodeHelper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    // 입력 값이 유효하지 않은 경우 (빈 값, 스페이스 값)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<Integer, List<String>> groupedErrors = new TreeMap<>();
        Map<String, String> globalFieldErrors = new LinkedHashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String key;
            if (error instanceof FieldError fieldError) {
                key = formatPathToSnakeCase(fieldError.getField());
            } else {
                key = extractObjectErrorKey(error);
            }

            String message = error.getDefaultMessage();

            if (key != null && key.contains("items[")) {
                int start = key.indexOf("[") + 1;
                int end = key.indexOf("]");
                int index = Integer.parseInt(key.substring(start, end)); // 0부터 시작하는 인덱스

                groupedErrors.computeIfAbsent(index, k -> new ArrayList<>()).add(message);
            } else {
                globalFieldErrors.put(key, message);
            }
        });

        List<Map<String, Object>> itemsList = new ArrayList<>();
        groupedErrors.forEach((index, errorList) -> {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("id", index);
            itemMap.put("errors", errorList);
            itemsList.add(itemMap);
        });

        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("items", itemsList);
        if (!globalFieldErrors.isEmpty()) {
            responseData.put("global", globalFieldErrors);
        }

        ApiResponse<Map<String, Object>> response = ApiResponse.fail(
                HttpStatus.BAD_REQUEST.value(),
                "입력값 검증 실패",
                responseData
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private String formatPathToSnakeCase(String fieldPath) {
        if (fieldPath == null || fieldPath.isBlank()) {
            return fieldPath;
        }

        String[] parts = fieldPath.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].contains("[")) {
                int bracketIdx = parts[i].indexOf("[");
                String fieldName = parts[i].substring(0, bracketIdx);
                String indexPart = parts[i].substring(bracketIdx);
                parts[i] = toSnakeCase(fieldName) + indexPart;
            } else {
                parts[i] = toSnakeCase(parts[i]);
            }
        }
        return String.join(".", parts);
    }

    private String extractObjectErrorKey(ObjectError error) {
        if (error.getCodes() != null) {
            for (String code : error.getCodes()) {
                if (code.contains("[")) {
                    String targetPath = code.substring(code.lastIndexOf(".") + 1);
                    return formatPathToSnakeCase(targetPath);
                }
            }
        }
        return toSnakeCase(error.getObjectName());
    }

    private String toSnakeCase(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    // 날짜 형식이 올바르지 않은 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {

        Map<String, String> errors = new LinkedHashMap<>();
        String message = "요청 데이터 형식이 올바르지 않습니다.";

        if (e.getCause() instanceof InvalidFormatException ife) {
            if (!ife.getPath().isEmpty()) {
                String fieldName = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
                String key = toSnakeCase(fieldName);
                errors.put(key, "올바른 형식(예: YYYY-MM-DD)의 날짜 또는 데이터 타입이어야 합니다.");
            }
        }

        ApiResponse<Map<String, String>> response = ApiResponse.fail(
                HttpStatus.BAD_REQUEST.value(),
                message,
                errors.isEmpty() ? null : errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
