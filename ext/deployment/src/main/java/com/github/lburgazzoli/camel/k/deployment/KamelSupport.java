package com.github.lburgazzoli.camel.k.deployment;

import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.quarkus.kubernetes.spi.DecoratorBuildItem;
import org.apache.camel.v1.IntegrationFluent;

import java.util.function.BiConsumer;

public final class KamelSupport {
    private KamelSupport() {
    }



    public static DecoratorBuildItem decorator(String name, BiConsumer<IntegrationFluent<?>, ObjectMeta> consumer) {
        return new DecoratorBuildItem(KamelConstants.KAMEL, new Applier(name, consumer));
    }

    public static class Applier extends NamedResourceDecorator<IntegrationFluent<?>> {
        final BiConsumer<IntegrationFluent<?>, ObjectMeta> consumer;

        public Applier(String name, BiConsumer<IntegrationFluent<?>, ObjectMeta> consumer) {
            super(KamelConstants.KAMEL_INTEGRATION, name);

            this.consumer = consumer;
        }

        @Override
        public void andThenVisit(IntegrationFluent<?> integration, ObjectMeta meta) {
            consumer.accept(integration, meta);
        }
    }
}
