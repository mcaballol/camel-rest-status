package org.example;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class StatusRoute extends RouteBuilder {
    @Override
    public void configure() {
        from("platform-http:/status?httpMethodRestrict=GET")
            .routeId("status-route")
            .process(exchange -> {
                String podName = System.getenv().getOrDefault("POD_NAME", "unknown");
                String namespace = System.getenv().getOrDefault("POD_NAMESPACE", "default");
                String message = System.getenv().getOrDefault("CUSTOM_MESSAGE", "Hola desde Camel Quarkus!");

                Map<String, String> body = new HashMap<>();
                body.put("podName", podName);
                body.put("namespace", namespace);
                body.put("message", message);

                exchange.getMessage().setBody(body);
            });
    }
}
