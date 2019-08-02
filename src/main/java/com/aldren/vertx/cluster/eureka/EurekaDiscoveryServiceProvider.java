package com.aldren.vertx.cluster.eureka;

import com.hazelcast.spi.discovery.integration.DiscoveryService;
import com.hazelcast.spi.discovery.integration.DiscoveryServiceProvider;
import com.hazelcast.spi.discovery.integration.DiscoveryServiceSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EurekaDiscoveryServiceProvider implements DiscoveryServiceProvider {

    @Autowired
    private EurekaDiscoveryService eurekaDiscoveryService;

    @Override
    public DiscoveryService newDiscoveryService(DiscoveryServiceSettings discoveryServiceSettings) {
        return eurekaDiscoveryService;
    }
}
