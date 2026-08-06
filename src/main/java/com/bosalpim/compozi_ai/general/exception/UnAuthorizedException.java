package com.bosalpim.compozi_ai.general.exception;

import com.bosalpim.compozi_ai.general.enums.BadStatusCode;

public class UnAuthorizedException extends CustomException {
    public UnAuthorizedException(BadStatusCode badStatusCode) {
        super(badStatusCode);
    }
}
