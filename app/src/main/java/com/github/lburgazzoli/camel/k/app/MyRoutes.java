package com.github.lburgazzoli.camel.k.app;

import org.apache.camel.builder.RouteBuilder;

public class MyRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("timer:tick")
                .to("log:info");

    }
}
