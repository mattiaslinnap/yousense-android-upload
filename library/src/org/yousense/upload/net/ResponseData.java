package org.yousense.upload.net;

import org.yousense.upload.exceptions.ServerUnhappyException;

public class ResponseData {
    public boolean success;
    public String error;
    public Upgrade upgrade_required;

    public static class Upgrade {
        public String url;
        public String whats_new;
    }

    /**
     * Throws exceptions unless everything is OK.
     */
    public void assertSuccess() throws ServerUnhappyException {
        if (upgrade_required != null)
            throw new ServerUnhappyException("Client upgrade required.");
        if (error != null)
            throw new ServerUnhappyException("Request rejected by server: " + error);
        if (!success)
            throw new ServerUnhappyException("Unknown server error.");
    }
}
