package org.yousense.upload.exceptions;

public class ClientVersionException extends Exception {
    public String url;
    public String whatsNew;

    public ClientVersionException(String url, String whatsNew) {
        this.url = url;
        this.whatsNew = whatsNew;
    }
}
