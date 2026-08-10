package com.bosalpim.compozi_ai.domain.document.component.parser;

import com.bosalpim.compozi_ai.domain.document.component.validator.ItemSpecAndUnitValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE}) // 클래스 단위에 부착
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ItemSpecAndUnitValidator.class)
@Documented
public @interface ValidItemSpecAndUnit {

    String message() default "규격 또는 단위 형식이 올바르지 않습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
