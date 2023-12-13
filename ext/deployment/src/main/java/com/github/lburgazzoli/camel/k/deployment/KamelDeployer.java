package com.github.lburgazzoli.camel.k.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.kubernetes.client.spi.KubernetesClientBuildItem;
import io.quarkus.kubernetes.deployment.KubernetesDeploy;
import io.quarkus.kubernetes.deployment.SelectedKubernetesDeploymentTargetBuildItem;
import io.quarkus.kubernetes.spi.GeneratedKubernetesResourceBuildItem;
import io.quarkus.kubernetes.spi.KubernetesDeploymentClusterBuildItem;

import java.util.List;
import java.util.Optional;

public class KamelDeployer {


    @BuildStep
    public void checkEnvironment(
             Optional<SelectedKubernetesDeploymentTargetBuildItem> selectedDeploymentTarget,
             List<GeneratedKubernetesResourceBuildItem> resources,
             KubernetesClientBuildItem kubernetesClientBuilder,
             BuildProducer<KubernetesDeploymentClusterBuildItem> deploymentCluster) {

        selectedDeploymentTarget
                .filter(target -> target.getEntry().getName().equals(KamelConstants.KAMEL))
                .ifPresent(target -> {

            if (!KubernetesDeploy.INSTANCE.checkSilently(kubernetesClientBuilder)) {
                return;
            }

            try (var client = kubernetesClientBuilder.buildClient()) {
                if (client.hasApiGroup("camel.apache.org", false)) {
                    deploymentCluster.produce(new KubernetesDeploymentClusterBuildItem(KamelConstants.KAMEL));
                } else {
                    throw new IllegalStateException(
                        "Kamel was requested as a deployment, but the target cluster is not a Kamel cluster!");
                }
            }
        });
    }
}
