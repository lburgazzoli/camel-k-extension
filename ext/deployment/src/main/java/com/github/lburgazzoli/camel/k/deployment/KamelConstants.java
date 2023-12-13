package com.github.lburgazzoli.camel.k.deployment;

import static io.quarkus.kubernetes.spi.KubernetesDeploymentTargetBuildItem.DEFAULT_PRIORITY;

public final class KamelConstants {


    public static final int KAMEL_PRIORITY = DEFAULT_PRIORITY;

    public static final String KAMEL = "kamel";
    public static final String KAMEL_INTEGRATION = "Integration";
    public static final String KAMEL_INTEGRATION_GROUP = "camel.apache.org";
    public static final String KAMEL_INTEGRATION_VERSION = "v1";


    private KamelConstants() {

    }
}
