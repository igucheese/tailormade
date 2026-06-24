package com.tailormade.tailor.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tailormade.tailor.client.model.MannequinModel;
import com.tailormade.tailor.entities.blockentities.MannequinEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import static com.tailormade.tailor.Tailormade.MODID;

public class MannequinRenderer extends LivingEntityRenderer<MannequinEntity, MannequinModel> {
    private static final ResourceLocation TRANSPARENT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/mannequin_empty.png");

    public MannequinRenderer(EntityRendererProvider.Context ctx) {
        super(
                ctx,
                new MannequinModel(ctx.bakeLayer(MannequinModel.LAYER_LOCATION)),
                0.0F
        );
        addLayer(new MannequinTailorRenderLayer(this, ctx));
    }

    @Override
    public void render(MannequinEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource,
                LightTexture.pack(15, 15));
    }

    @Override
    public ResourceLocation getTextureLocation(MannequinEntity entity) {
        return TRANSPARENT_TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(MannequinEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected float getShadowRadius(MannequinEntity entity) {
        return 0F;
    }
}
