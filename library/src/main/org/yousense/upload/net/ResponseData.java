package org.yousense.upload.net;

public class ResponseData {
    public Update update_required;
    public String error;

    public static class Update {
        public String url;
        public String whats_new;
    }
}
