package org.yousense.upload.net;

public class ResponseData {
    public Upgrade upgrade_required;
    public String error;

    public static class Upgrade {
        public String url;
        public String whats_new;
    }
}
