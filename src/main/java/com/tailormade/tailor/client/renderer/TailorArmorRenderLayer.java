package com.tailormade.tailor.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tailormade.tailor.utils.MannequinStylePreviewHelper;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

public class TailorArmorRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static ResourceLocation previewOverride = null;

    public static void setPreviewOverride(ResourceLocation tex) { previewOverride = tex; }
    public static void clearPreviewOverride() { previewOverride = null; }

    public TailorArmorRenderLayer(
        RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ResourceLocation texture = resolveTexture(player);
        if (texture == null) return;
        if (MannequinStylePreviewHelper.isHideArmor()) { return; }

        getParentModel().renderToBuffer(
                poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucentCull(texture)),
                packedLight,
                OverlayTexture.NO_OVERLAY
        );
    }

    private ResourceLocation resolveTexture(AbstractClientPlayer player) {
        if (previewOverride != null) return previewOverride;

        TailorTextureCompositor compositor =
                TailorTextureCompositor.getOrCreate(player.getUUID());
        return compositor.getOrUpdate(player);
    }
}
