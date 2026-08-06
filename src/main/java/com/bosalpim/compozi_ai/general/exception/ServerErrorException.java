package com.bosalpim.compozi_ai.general.exception;

import com.bosalpim.compozi_ai.general.enums.BadStatusCode;

public class ServerErrorException extends CustomException {
    public ServerErrorException(BadStatusCode badStatusCode) {
        super(badStatusCode);
    }
}
