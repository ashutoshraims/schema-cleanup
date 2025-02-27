package org.opengroup.osdu.azure.cleanup.exceptionhandler;


public record ErrorResponse(
        int code,
        String message,
        String reason) {

}
