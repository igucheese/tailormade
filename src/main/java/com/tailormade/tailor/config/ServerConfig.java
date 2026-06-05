package com.tailormade.tailor.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("Tailormade Server Config");
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
