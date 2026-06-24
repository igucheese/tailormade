package com.tailormade.tailor.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.MannequinStylePreviewHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class TailorHumanoidArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, M, A> {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public TailorHumanoidArmorLayer(
            RenderLayerParent<T, M> renderer,
            A innerModel,
            A outerModel,
            ModelManager modelManager
    ) {
        super(renderer, innerModel, outerModel, modelManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            A model = (A)(slot == EquipmentSlot.LEGS ? innerModel : outerModel);
            renderArmorPiece(poseStack, bufferSource, entity, slot, packedLight, model);
        }
    }

    @Override
    protected void renderArmorPiece(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            T entity,
            EquipmentSlot slot,
            int packedLight,
            A model
    ) {
        if (MannequinStylePreviewHelper.isHideArmor()) return;
        ItemStack stack = entity.getItemBySlot(slot);
        if (!stack.isEmpty() && stack.has(ModDataComponents.PATTERN_ID.get())) {
            return;
        }

        super.renderArmorPiece(poseStack, bufferSource, entity, slot, packedLight, model);
    }
}
