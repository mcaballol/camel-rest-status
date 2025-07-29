package com.example.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class StatusRoute extends RouteBuilder {
    @Override
    public void configure() {
        restConfiguration().component("servlet");

        rest("/status")
            .get()
            .produces("application/json")
            .to("direct:status-handler");

        from("direct:status-handler")
            .process(exchange -> {
                String podName = System.getenv().getOrDefault("POD_NAME", "unknown");
                String podNamespace = System.getenv().getOrDefault("POD_NAMESPACE", "default");
                String message = System.getenv().getOrDefault("CUSTOM_MESSAGE", "Hola desde Camel!");

                Map<String, String> body = new HashMap<>();
                body.put("podName", podName);
                body.put("namespace", podNamespace);
                body.put("message", message);

                exchange.getMessage().setBody(body);
            });
    }
}
