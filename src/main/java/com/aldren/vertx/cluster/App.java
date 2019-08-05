package com.aldren.vertx.cluster;

import com.aldren.vertx.cluster.dispatcher.ApiDispatcher;
import com.aldren.vertx.cluster.http.ApiServer;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class App 
{

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApiDispatcher dispatcher;

    @Autowired
    private ApiServer server;

    public static void main( String[] args )
    {
        SpringApplication.run(App.class,args);
    }

    @PostConstruct
    public void init() {
        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);
        Vertx.clusteredVertx(options.setEventLoopPoolSize(2), cluster -> {
            if(cluster.succeeded()) {
                Vertx vertx = cluster.result();
                vertx.deployVerticle(server);
                vertx.deployVerticle(dispatcher);
            }
        });
    }

}
