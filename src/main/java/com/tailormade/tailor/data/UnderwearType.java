package com.tailormade.tailor.data;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

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
    public static UnderwearType getType(String key) {
        return switch (key) {
            case "male_boxer"  -> MALE_BOXER;
            case "male_bikini" -> MALE_BIKINI;
            case "female_boxer"  -> FEMALE_BOXER;
            case "female_bikini"  -> FEMALE_BIKINI;
            default -> MALE_BOXER;
        };
    }
    public String getTextureKey() { return this.textureKey; }

    public Component getLabel() {
        return Component.translatable("underwear.tailormade." + textureKey);
    }
}

