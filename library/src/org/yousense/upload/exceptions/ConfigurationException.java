package org.yousense.upload.exceptions;

public class ConfigurationException extends Exception {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable tr) {
        super(message, tr);
    }
}
