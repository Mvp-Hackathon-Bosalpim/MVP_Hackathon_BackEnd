package com.bosalpim.compozi_ai.general.exception;

import com.bosalpim.compozi_ai.general.enums.BadStatusCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final BadStatusCode badStatusCode;

    public CustomException(BadStatusCode badStatusCode) {
        super(badStatusCode.getMessage());
        this.badStatusCode = badStatusCode;
    }
}
