package com.bosalpim.compozi_ai.general.util;

import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class StatusCodeHelper {

    private final int statusValue;
    private final HttpStatus httpStatus;
    private final String message;

    public static StatusCodeHelper of(BadStatusCode code) {
        return new StatusCodeHelper(code.getHttpStatus().value(), code.getHttpStatus(), code.getMessage());
    }

}
