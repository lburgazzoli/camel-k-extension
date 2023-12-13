package com.github.lburgazzoli.camel.k.deployment;

import static io.quarkus.kubernetes.spi.KubernetesDeploymentTargetBuildItem.DEFAULT_PRIORITY;

public final class KamelConstants {


    static final int KAMEL_PRIORITY = DEFAULT_PRIORITY;

    static final String KAMEL = "kamel";
    static final String KAMEL_INTEGRATION = "Integration";
    static final String KAMEL_INTEGRATION_GROUP = "camel.apache.org";
    static final String KAMEL_INTEGRATION_VERSION = "v1";


    private KamelConstants() {

    }
}
