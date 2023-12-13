package com.github.lburgazzoli.camel.k.deployment.dekorate;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.ManifestGeneratorFactory;
import io.dekorate.ResourceRegistry;

public class KamelManifestGeneratorFactory implements ManifestGeneratorFactory {

    @Override
    public KamelManifestGenerator create(ResourceRegistry resourceRegistry, ConfigurationRegistry configurationRegistry) {
        return new KamelManifestGenerator(resourceRegistry, configurationRegistry);
    }
}