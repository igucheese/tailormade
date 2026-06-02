package com.tailormade.tailor.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tailormade.tailor.client.model.MannequinModel;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.entities.blockentities.MannequinEntity;
import com.tailormade.tailor.registries.ModDataComponents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.EnumMap;
import java.util.Map;

import static com.tailormade.tailor.utils.DesignAccessor.getPixelDataFromId;

public class MannequinTailorRenderLayer
        extends RenderLayer<MannequinEntity, MannequinModel> {

    public MannequinTailorRenderLayer(
            RenderLayerParent<MannequinEntity, MannequinModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, MannequinEntity entity,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        Map<PatternType, int[]> pixelMap = collectPixelData(entity);
        if (pixelMap.isEmpty()) return;

        // エンティティごとにコンポジターを持つ（UUID ベースのキャッシュを流用）
        TailorTextureCompositor compositor =
                TailorTextureCompositor.getOrCreate(entity.getUUID());

        ResourceLocation texture = compositor.composeForPreview(pixelMap);
        if (texture == null) return;

        getParentModel().renderToBuffer(
                poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucentCull(texture)),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF
        );
    }

    private Map<PatternType, int[]> collectPixelData(MannequinEntity entity) {
        Map<PatternType, int[]> map = new EnumMap<>(PatternType.class);
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {

            var stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

//            var pd = stack.get(ModDataComponents.PIXEL_DATA.get());
            var pd = getPixelDataFromId(stack.get(ModDataComponents.PATTERN_ID.get()));
            if (pd == null) continue;

            PatternType type = switch (slot) {
                case HEAD  -> PatternType.HEAD;
                case CHEST -> PatternType.CHEST;
                case LEGS  -> PatternType.LEGS;
                case FEET  -> PatternType.FEET;
                default    -> null;
            };
            if (type != null) map.put(type, pd.pixels());
        }
        return map;
    }
}
