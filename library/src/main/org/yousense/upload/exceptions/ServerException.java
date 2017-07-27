package org.yousense.upload.exceptions;

import java.io.IOException;

/**
 *
 */
public class ServerException extends IOException {
    public ServerException(int statusCode, String message) {
        super("Server Error " + statusCode + ": " + message);
    }

    public ServerException(int statusCode, String message, Throwable tr) {
        super("Server Error " + statusCode + ": " + message, tr);
    }
}
