package com.aldren.vertx.cluster.http;

import com.aldren.vertx.cluster.constants.CommonConstants;
import com.aldren.vertx.cluster.handler.FailureHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Component
public class ApiServer extends AbstractVerticle {

    @Autowired
    private FailureHandler failureHandler;

    private Optional<HttpServer> server = Optional.empty();

    private Map<String, RoutingContext> mapping = new HashMap<>();

    private String uid;

    @Override
    public void start(Future<Void> future) throws Exception {
        server = Optional.of(getVertx().createHttpServer().requestHandler(routerConfig()::accept));
        server.get().listen(8080, result -> {
            if(result.succeeded()) {
                future.complete();
            } else {
                future.fail(result.cause());
            }
        });
        vertx.eventBus().localConsumer(CommonConstants.API_ADDRESS).handler(this::handleResponse);
    }

    private Router routerConfig() {
        Router router = Router.router(vertx);

        router.route().produces("application/json");
        router.route().handler(BodyHandler.create());

        Set<HttpMethod> methods = new HashSet<>();
        methods.add(HttpMethod.GET);

        router.route().handler(CorsHandler.create("*").allowedMethods(methods));

        router.route().handler(routingContext -> {
            routingContext.response().headers().add("Content-Type","application/json");
            routingContext.next();
        });

        router.route().failureHandler(failureHandler);

        router.get(CommonConstants.API_ENDPOINT).handler(this::handleRequest);

        return router;
    }

    private void handleRequest(RoutingContext routingContext) {
        InetAddress ip;
        String ipAddress = "";
        try {
            ip = InetAddress.getLocalHost();
            ipAddress = ip.toString();
        } catch (UnknownHostException e) {
            routingContext.fail(e);
        }
        JsonObject obj = new JsonObject();
        uid = ipAddress + "*" + UUID.randomUUID().toString();
        obj.put("UUID", uid);
        timeout(uid,3000);
        mapping.put(uid, routingContext);
        vertx.eventBus().send(CommonConstants.DISPATCHER_ADDRESS, obj);
    }

    private void handleResponse(Message<Object> response) {
        Optional.ofNullable(mapping.remove(uid)).ifPresent(ctx -> {
            ctx.response().end("done");
        });
    }

    private void timeout(String uid, int milliseconds) {
        vertx.setTimer(milliseconds, timer -> {
            Optional.ofNullable(mapping.remove(uid)).ifPresent(this::closeConnection);
        });
    }

    private void closeConnection(RoutingContext context) {
        context.fail(new Exception());
        context.next();
    }

}
