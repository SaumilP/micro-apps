package org.sandcastle.apps.handler;

import com.networknt.handler.LightHttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint handler
 * Returns application health status
 */
public class HealthHandler implements LightHttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "light4j-rest-app");
        response.put("timestamp", System.currentTimeMillis());

        exchange.setStatusCode(StatusCodes.OK);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(toJson(response));
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (i > 0) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            i++;
        }
        json.append("}");
        return json.toString();
    }
}
