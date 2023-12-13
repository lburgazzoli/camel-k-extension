package com.github.lburgazzoli.camel.k.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

public final class KamelBuildItem extends SimpleBuildItem {
    private final boolean enabled;
    private final String name;

    public KamelBuildItem(boolean enabled, String name) {
        this.enabled = enabled;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
