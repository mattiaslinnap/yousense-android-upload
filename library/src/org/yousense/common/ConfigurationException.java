package org.yousense.common;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable tr) {
        super(message, tr);
    }
}
