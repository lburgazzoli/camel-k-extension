package com.github.lburgazzoli.camel.k.deployment;


import io.quarkus.kubernetes.spi.DeployStrategy;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.util.Optional;

@ConfigRoot(prefix = "kamel")
public class KamelConfig {

    /**
     * The name of the application. This value will be used for naming Kubernetes
     * resources like: - Deployment - Service and so on ...
     */
    @ConfigItem
    Optional<String> name;

    /**
     * If deploy is enabled, it will follow this strategy to update the resources to the target Kamel cluster.
     */
    @ConfigItem(defaultValue = "CreateOrUpdate")
    DeployStrategy deployStrategy;

}
