package com.example.mini_dynamite.cluster;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ClusterConfiguration {

    @Bean
    public ConsistentHashRing consistentHashRing(
            @Value("${cluster.virtual-nodes:50}")
            int virtualNodes,
            @Value("${cluster.nodes}")
            List<String> nodeDefinitions
    ){
        // Initialise ConsistentHashRing class with virtualNodes count from properties.yml
        ConsistentHashRing ring =
                new ConsistentHashRing(virtualNodes);

        // For each nodeDefinitions from properties.yml
        for (String definition : nodeDefinitions) {

            String[] parts = definition.split("\\|", 2);

            // if nodeDefinition format is not correct
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid cluster node: " + definition
                );
            }
            // create ClusterNode and add to ring
            ring.addNode(
                    new ClusterNode(parts[0], parts[1])
            );
        }

        return ring;
    }
}
