package com.bosalpim.compozi_ai.general.response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.HttpStatus;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiSuccess {
    HttpStatus statusCode() default HttpStatus.OK;

    String message() default "요청에 성공 했습니다.";

}
