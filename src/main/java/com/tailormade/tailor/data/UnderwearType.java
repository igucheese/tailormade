package com.tailormade.tailor.data;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.tailormade.tailor.Tailormade.MODID;

public enum UnderwearType {
    MALE_BOXER  ("male_boxer",   false),
    MALE_BIKINI ("male_bikini",  false),
    FEMALE_BOXER("female_boxer", true),
    FEMALE_BIKINI("female_bikini", true);

    private final String textureKey;
    private final boolean coversChest;

    UnderwearType(String textureKey, boolean coversChest) {
        this.textureKey   = textureKey;
        this.coversChest  = coversChest;
    }

    public ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/underwear/" + textureKey + ".png");
    }

    public boolean coversChest() { return coversChest; }

    public Component getLabel() {
        return Component.translatable("underwear.tailormade." + textureKey);
    }
}

