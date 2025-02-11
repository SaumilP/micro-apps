package org.sandcastle.apps.web;

import java.util.Map;

import org.sandcastle.apps.framework.annotations.Autowired;
import org.sandcastle.apps.framework.annotations.Component;
import org.sandcastle.apps.framework.annotations.RequestMapping;
import org.sandcastle.apps.framework.http.HttpRequest;
import org.sandcastle.apps.framework.http.HttpResponse;

@Component
public class SimpleController {
    @Autowired
    private HiService hiService;

    @RequestMapping("/hi")
    public HttpResponse handleRequest(HttpRequest request) {
        hiService.pringMessage();
        Map<String, Object> responseBody = Map.of("message", "Hi There!");
        return new HttpResponse(200, responseBody);
    }
}
