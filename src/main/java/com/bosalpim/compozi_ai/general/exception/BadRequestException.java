package com.bosalpim.compozi_ai.general.exception;

import com.bosalpim.compozi_ai.general.enums.BadStatusCode;

public class BadRequestException extends CustomException {
    public BadRequestException(BadStatusCode badStatusCode) {
        super(badStatusCode);
    }
}
