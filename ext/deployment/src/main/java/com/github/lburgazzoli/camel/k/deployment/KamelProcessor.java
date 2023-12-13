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
import org.apache.camel.v1.IntegrationFluent;
import org.apache.camel.v1.integrationspec.TraitsFluent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


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
                    config.name.orElseGet(applicationInfo::getName))
            );
        }
    }



    @BuildStep
    public List<DecoratorBuildItem> createDecorators(
        ApplicationInfoBuildItem applicationInfo,
        OutputTargetBuildItem outputTarget,
        KamelConfig config,
        PackageConfig packageConfig,
        Optional<MetricsCapabilityBuildItem> metricsConfiguration,
        Optional<KubernetesClientCapabilityBuildItem> kubernetesClientConfiguration,
        List<KubernetesAnnotationBuildItem> annotations,
        List<KubernetesLabelBuildItem> labels,
        List<KubernetesEnvBuildItem> envs,
        Optional<BaseImageInfoBuildItem> baseImage,
        Optional<ContainerImageInfoBuildItem> image,
        Optional<KubernetesCommandBuildItem> command,
        List<KubernetesPortBuildItem> ports,
        Optional<KubernetesHealthLivenessPathBuildItem> livenessPath,
        Optional<KubernetesHealthReadinessPathBuildItem> readinessPath,
        Optional<KubernetesHealthStartupPathBuildItem> startupProbePath,
        List<KubernetesRoleBuildItem> roles,
        List<KubernetesClusterRoleBuildItem> clusterRoles,
        List<KubernetesServiceAccountBuildItem> serviceAccounts,
        List<KubernetesRoleBindingBuildItem> roleBindings,
        Optional<CustomProjectRootBuildItem> customProjectRoot,
        List<KubernetesDeploymentTargetBuildItem> targets) {

        List<DecoratorBuildItem> result = new ArrayList<>();

        boolean enabled = targets.stream()
                .filter(KubernetesDeploymentTargetBuildItem::isEnabled)
                .map(KubernetesDeploymentTargetBuildItem::getName)
                .anyMatch(KamelConstants.KAMEL::equals);

        if (!enabled) {
            return result;
        }


        Optional<Project> project = KubernetesCommonHelper.createProject(applicationInfo, customProjectRoot, outputTarget, packageConfig);
        String name = config.name.orElseGet(applicationInfo::getName);

        image.ifPresent(i -> {
            result.add(new DecoratorBuildItem(KamelConstants.KAMEL, new ApplyTraitToIntegrationTemplate(t -> {
                t.editOrNewContainer().withImage(i.getImage());
            })));
        });

        result.add(new DecoratorBuildItem(KamelConstants.KAMEL, new ApplyTraitToIntegrationTemplate(t -> {
            t.editOrNewIngress().withEnabled(true);
        })));

        return result;
    }

    public class ApplyTraitToIntegrationTemplate extends NamedResourceDecorator<IntegrationFluent<?>> {
        final Consumer<TraitsFluent<?>> consumer;

        public ApplyTraitToIntegrationTemplate(Consumer<TraitsFluent<?>> consumer) {
            super(KamelConstants.KAMEL_INTEGRATION);

            this.consumer = consumer;
        }

        @Override
        public void andThenVisit(IntegrationFluent<?> integration, ObjectMeta objectMeta) {
            var t = integration.editOrNewSpec().editOrNewTraits();

            this.consumer.accept(t);
            t.endIntegrationspecTraits().endSpec();
        }
    }
}
