package org.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class RestRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        //restConfiguration().bindingMode(RestBindingMode.json);
        rest("/info")
            .get()
            .produces("application/json")
            .to("direct:info");
        from("direct:info")
            .setHeader("PodName", simple("${env:POD_NAME}"))
            .setHeader("Namespace", simple("${env:POD_NAMESPACE}"))
            .setHeader("PodIP", simple("${env:POD_IP}"))
            .setHeader("CustomMessage", simple("${env:CUSTOM_MESSAGE}"))
            .setBody().simple("""
                {
                  "podName": "${header.PodName}",
                  "namespace": "${header.Namespace}",
                  "podIP": "${header.PodIP}",
                  "message": "${header.CustomMessage}"
                }
                """);
    }
}

