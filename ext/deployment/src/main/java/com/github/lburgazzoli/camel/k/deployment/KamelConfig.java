package com.github.lburgazzoli.camel.k.deployment;


import io.quarkus.kubernetes.spi.DeployStrategy;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.util.Optional;

@ConfigRoot(prefix = "quarkus.kamel")
public class KamelConfig {

    /**
     * The name of the application. This value will be used for naming Kubernetes
     * resources like: - Deployment - Service and so on ...
     */
    @ConfigItem
    Optional<String> name;

    /**
     * The namespace the generated resources should belong to.
     * If not value is set, then the 'namespace' field will not be
     * added to the 'metadata' section of the generated manifests.
     * This in turn means that when the manifests are applied to a cluster,
     * the namespace will be resolved from the current Kubernetes context
     * (see https://kubernetes.io/docs/concepts/configuration/organize-cluster-access-kubeconfig/#context
     * for more details).
     */
    @ConfigItem
    Optional<String> namespace;

    /**
     * If set to true, Quarkus will attempt to deploy the application to the target knative cluster
     */
    @ConfigItem(defaultValue = "false")
    boolean deploy;

    /**
     * If deploy is enabled, it will follow this strategy to update the resources to the target Kamel cluster.
     */
    @ConfigItem(defaultValue = "CreateOrUpdate")
    DeployStrategy deployStrategy;

}
