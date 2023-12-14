package com.github.lburgazzoli.camel.k.deployment;

import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.dekorate.project.Project;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.quarkus.container.spi.BaseImageInfoBuildItem;
import io.quarkus.container.spi.ContainerImageInfoBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ApplicationInfoBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.metrics.MetricsCapabilityBuildItem;
import io.quarkus.deployment.pkg.PackageConfig;
import io.quarkus.deployment.pkg.builditem.OutputTargetBuildItem;
import io.quarkus.kubernetes.client.spi.KubernetesClientCapabilityBuildItem;
import io.quarkus.kubernetes.deployment.KubernetesCommonHelper;
import io.quarkus.kubernetes.deployment.KubernetesConfigUtil;
import io.quarkus.kubernetes.spi.CustomProjectRootBuildItem;
import io.quarkus.kubernetes.spi.DecoratorBuildItem;
import io.quarkus.kubernetes.spi.KubernetesAnnotationBuildItem;
import io.quarkus.kubernetes.spi.KubernetesClusterRoleBuildItem;
import io.quarkus.kubernetes.spi.KubernetesCommandBuildItem;
import io.quarkus.kubernetes.spi.KubernetesDeploymentTargetBuildItem;
import io.quarkus.kubernetes.spi.KubernetesEnvBuildItem;
import io.quarkus.kubernetes.spi.KubernetesHealthLivenessPathBuildItem;
import io.quarkus.kubernetes.spi.KubernetesHealthReadinessPathBuildItem;
import io.quarkus.kubernetes.spi.KubernetesHealthStartupPathBuildItem;
import io.quarkus.kubernetes.spi.KubernetesLabelBuildItem;
import io.quarkus.kubernetes.spi.KubernetesPortBuildItem;
import io.quarkus.kubernetes.spi.KubernetesResourceMetadataBuildItem;
import io.quarkus.kubernetes.spi.KubernetesRoleBindingBuildItem;
import io.quarkus.kubernetes.spi.KubernetesRoleBuildItem;
import io.quarkus.kubernetes.spi.KubernetesServiceAccountBuildItem;
import io.quarkus.runtime.annotations.ConfigItem;
import org.apache.camel.v1.IntegrationFluent;
import org.apache.camel.v1.integrationspec.TraitsFluent;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.github.lburgazzoli.camel.k.deployment.KamelSupport.decorator;

public class KamelProcessor {
    private static final String FEATURE = "kamel-extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void deploymentTarget(
            ApplicationInfoBuildItem applicationInfo,
            KamelConfig config,
            BuildProducer<KubernetesDeploymentTargetBuildItem> deploymentTargets,
            BuildProducer<KubernetesResourceMetadataBuildItem> resourceMeta) {

        List<String> targets = KubernetesConfigUtil.getConfiguredDeploymentTargets();
        boolean enabled = targets.contains(KamelConstants.KAMEL);

        deploymentTargets.produce(
                new KubernetesDeploymentTargetBuildItem(
                        KamelConstants.KAMEL,
                        KamelConstants.KAMEL_INTEGRATION,
                        KamelConstants.KAMEL_INTEGRATION_GROUP,
                        KamelConstants.KAMEL_INTEGRATION_VERSION,
                        KamelConstants.KAMEL_PRIORITY,
                        enabled,
                        config.deployStrategy));

        if (enabled) {
            resourceMeta.produce(new KubernetesResourceMetadataBuildItem(
                    KamelConstants.KAMEL,
                    KamelConstants.KAMEL_INTEGRATION_GROUP,
                    KamelConstants.KAMEL_INTEGRATION_VERSION,
                    KamelConstants.KAMEL_INTEGRATION,
                    config.name.orElseGet(applicationInfo::getName)));
        }
    }

    @BuildStep
    public KamelBuildItem kamel(
            ApplicationInfoBuildItem applicationInfo,
            KamelConfig config,
            List<KubernetesDeploymentTargetBuildItem> targets) {

        boolean enabled = targets.stream()
                .filter(KubernetesDeploymentTargetBuildItem::isEnabled)
                .map(KubernetesDeploymentTargetBuildItem::getName)
                .anyMatch(KamelConstants.KAMEL::equals);

        return new KamelBuildItem(
                enabled,
                config.name.orElseGet(applicationInfo::getName));
    }

    // ****************************************
    //
    // Traits
    //
    // ****************************************

    @BuildStep
    public void common(
            KamelBuildItem kamel,
            BuildProducer<DecoratorBuildItem> decorators) {

        if (!kamel.isEnabled()) {
            return;
        }

        decorators.produce(decorator(kamel.getName(), (integration, meta) -> {
            integration.editOrNewMetadata()
                    .withNamespace(ConfigProvider.getConfig()
                            .getOptionalValue("quarkus.kubernetes.namespace", String.class).orElse("default"))
                    .endMetadata();
        }));
    }

    @BuildStep
    public void container(
            KamelBuildItem kamel,
            BuildProducer<DecoratorBuildItem> decorators,
            Optional<ContainerImageInfoBuildItem> image) {

        if (!kamel.isEnabled()) {
            return;
        }

        image.ifPresent(value -> {
            decorators.produce(decorator(kamel.getName(), (integration, meta) -> {
                integration.editOrNewSpec().editOrNewTraits()
                        .withNewContainer()
                        .withImage(value.getImage())
                        .withName(null)
                        .withPort(null)
                        .withPortName(null)
                        .withServicePort(null)
                        .withServicePortName(null)
                        .endTraitsContainer()
                        .endIntegrationspecTraits()
                        .endSpec();
            }));
        });
    }

    @BuildStep
    public void service(
            KamelBuildItem kamel,
            BuildProducer<DecoratorBuildItem> decorators) {

        if (!kamel.isEnabled()) {
            return;
        }

        decorators.produce(decorator(kamel.getName(), (integration, meta) -> {
            integration.editOrNewSpec().editOrNewTraits()
                    .withNewService()
                    .withEnabled(true)
                    .withAuto(false)
                    .withNodePort(null)
                    .endIntegrationspecService()
                    .endIntegrationspecTraits()
                    .endSpec();
        }));
    }
}
