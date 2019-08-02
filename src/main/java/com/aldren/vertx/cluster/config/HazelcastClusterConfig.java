package com.aldren.vertx.cluster.config;

import com.aldren.vertx.cluster.eureka.EurekaDiscoveryServiceProvider;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastClusterConfig {

    @Autowired
    private EurekaDiscoveryServiceProvider eurekaDiscoveryServiceProvider;

    @Bean
    public ClusterManager clusterManager() {
        Config hazelcastConfig = ConfigUtil.loadConfig();
        hazelcastConfig.getGroupConfig().setName("vertxcluster");
        hazelcastConfig.setProperty("hazelcast.discovery.enabled", Boolean.TRUE.toString());

        NetworkConfig network = hazelcastConfig.getNetworkConfig();

        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getDiscoveryConfig().setDiscoveryServiceProvider(eurekaDiscoveryServiceProvider);

        return new HazelcastClusterManager(hazelcastConfig);
    }

}
