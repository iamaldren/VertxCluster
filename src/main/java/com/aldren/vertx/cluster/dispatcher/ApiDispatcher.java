package com.aldren.vertx.cluster.dispatcher;

import com.aldren.vertx.cluster.constants.CommonConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ApiDispatcher extends AbstractVerticle {

    private String dispatchAddr;

    @Override
    public void start() throws Exception {
        initDispatchAddr();
        vertx.eventBus().localConsumer(CommonConstants.DISPATCHER_ADDRESS).handler(this::dispatch);
    }

    private void initDispatchAddr() {
        dispatchAddr = UUID.randomUUID().toString();
        vertx.eventBus().consumer(dispatchAddr).handler(this::sendResponse);
    }

    private void sendResponse(Message<Object> response) {
        vertx.eventBus().send(CommonConstants.API_ADDRESS, response);
    }

    private void dispatch(Message<Object> request) {
        JsonObject obj = (JsonObject) request.body();
        String uuid = obj.getString("UUID");

        //Insert to cluster map.
        vertx.sharedData().getClusterWideMap("cluster", result -> {
            result.result().put(uuid, dispatchAddr, success -> {
                if(result.succeeded()) {
                    System.out.println("UUID " + uuid + " successfully inserted to cluster map.");
                }
            });
        });

        //Iterate on cluster map, to see if data are clustered
        vertx.sharedData().getClusterWideMap("cluster", result -> {
            result.result().entries(handler -> {
                handler.result().keySet().forEach(key -> {
                    System.out.println("Key::" + key + ", Value::" + handler.result().get(key));
                });
            });
        });

        vertx.eventBus().send(dispatchAddr, "done");
    }

}
