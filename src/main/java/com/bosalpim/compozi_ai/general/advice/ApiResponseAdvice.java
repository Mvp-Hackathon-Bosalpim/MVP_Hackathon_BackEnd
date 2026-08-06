package com.bosalpim.compozi_ai.general.advice;

import com.bosalpim.compozi_ai.general.response.ApiResponse;
import com.bosalpim.compozi_ai.general.response.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private final MessageSource messageSource;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getMethodAnnotation(ApiSuccess.class) != null;
    }

    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ApiResponse) {
            return body;
        }

        ApiSuccess methodAnnotation = returnType.getMethodAnnotation(ApiSuccess.class);
        int httpStatus = methodAnnotation.statusCode().value();
        String messageCode = methodAnnotation.message();
        String message = messageSource.getMessage(messageCode, null, messageCode, LocaleContextHolder.getLocale());

        return ApiResponse.success(httpStatus, message, body);
    }
}
