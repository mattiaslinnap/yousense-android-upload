package org.yousense.upload;

public class ManifestException extends Exception {
    public ManifestException(String message) {
        super(message);
    }

    public ManifestException(String message, Throwable tr) {
        super(message, tr);
    }
}
