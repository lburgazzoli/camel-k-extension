package com.github.lburgazzoli.camel.k.deployment.dekorate;

import com.github.lburgazzoli.camel.k.deployment.KamelConstants;
import io.dekorate.AbstractKubernetesManifestGenerator;
import io.dekorate.ConfigurationRegistry;
import io.dekorate.ResourceRegistry;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.EditableKubernetesConfig;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import org.apache.camel.v1.Integration;
import org.apache.camel.v1.IntegrationBuilder;

import java.util.Optional;

public class KamelManifestGenerator extends AbstractKubernetesManifestGenerator<KubernetesConfig> {


    public KamelManifestGenerator() {
        this(new ResourceRegistry(), new ConfigurationRegistry());
    }

    public KamelManifestGenerator(ResourceRegistry resourceRegistry, ConfigurationRegistry configurationRegistry) {
        super(resourceRegistry, configurationRegistry);
    }

    @Override
    public int order() {
        return 600;
    }

    @Override
    public String getKey() {
        return KamelConstants.KAMEL;
    }

    @Override
    public void generate(KubernetesConfig config) {
        Optional<Integration> existingService = resourceRegistry.groups()
                .getOrDefault(KamelConstants.KAMEL, new KubernetesListBuilder())
                .buildItems().stream()
                .filter(Integration.class::isInstance)
                .map(Integration.class::cast)
                .filter(i -> i.getMetadata().getName().equals(config.getName()))
                .findAny();

        if (existingService.isEmpty()) {
            resourceRegistry.add(KamelConstants.KAMEL, createResource(config));
        }
    }

    public boolean accepts(Class<? extends Configuration> type) {
        return type.equals(KubernetesConfig.class) || type.equals(EditableKubernetesConfig.class);
    }

    public Integration createResource(KubernetesConfig config) {
        return new IntegrationBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(config.getName())
                        .build())
                .build();

    }
}
