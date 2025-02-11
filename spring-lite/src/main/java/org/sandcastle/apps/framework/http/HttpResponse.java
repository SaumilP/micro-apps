package org.sandcastle.apps.framework.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final int status;
    private final Map<String, Object> body;

    public HttpResponse() {
        this.status = 200;
        this.body = new HashMap<>();
    }

    public HttpResponse(int status, Map<String, Object> body) {
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, Object> getBody() {
        return body;
    }
}
