package com.aldren.vertx.cluster.eureka;

import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.hazelcast.spi.discovery.integration.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EurekaDiscoveryService implements DiscoveryService {

    @Value("${spring.application.name}")
    public String applicationName;

    @Value("${hazelcast.port}")
    public int port;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public void start() {

    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        List<DiscoveryNode> nodes = new ArrayList<>();

        discoveryClient.getInstances(applicationName).forEach(
                (ServiceInstance serviceInstance) -> {
                    try {
                        String host = serviceInstance.getHost();
                        if(host != null) {
                            Address address = new Address(host, Integer.valueOf(port));
                            DiscoveryNode discoveryNode = new SimpleDiscoveryNode(address);
                            nodes.add(discoveryNode);
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                });

        return nodes;
    }

    @Override
    public void destroy() {

    }

    @Override
    public Map<String, Object> discoverLocalMetadata() {
        return new HashMap<>();
    }
}
