package com.aldren.vertx.cluster.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.springframework.stereotype.Component;

@Component
public class FailureHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {
        if(routingContext.failed() && routingContext.failure() != null) {
            Throwable throwable = routingContext.failure();
            throwable.printStackTrace();

            routingContext.response().setStatusCode(500).end();
        }
    }
}
