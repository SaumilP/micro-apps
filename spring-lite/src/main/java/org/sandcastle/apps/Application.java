package org.sandcastle.apps;

import org.sandcastle.apps.framework.http.HttpRequest;
import org.sandcastle.apps.framework.server.LiteServer;
import org.sandcastle.apps.web.HiService;
import org.sandcastle.apps.web.SimpleController;

public class Application {
    public static void main(final String[] args) {
        var app = new LiteServer(SimpleController.class, HiService.class);
        var request = new HttpRequest("GET", "/hi");
        var response = app.handleRequest(request);
        System.out.println("➡️ Response: " + response.getBody());
    }
}