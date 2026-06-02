package com.tailormade.tailor.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tailormade.tailor.registries.ModDataComponents;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class TailorHumanoidArmorLayer<
        T extends LivingEntity,
        M extends HumanoidModel<T>,
        A extends HumanoidModel<T>>
        extends HumanoidArmorLayer<T, M, A> {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public TailorHumanoidArmorLayer(
            RenderLayerParent<T, M> renderer,
            A innerModel,
            A outerModel,
            ModelManager modelManager) {
        super(renderer, innerModel, outerModel, modelManager);
    }

    /**
     * super.render() を呼ばず、スロットごとに renderArmorPiece を明示的に呼ぶ。
     *
     * super.render() が内部で renderArmorPiece を呼ぶとき、
     * Java バイトコードは invokespecial（非仮想）になるためオーバーライドが効かない。
     * ここでは this.renderArmorPiece() と呼ぶことで invokevirtual になり、
     * 下の renderArmorPiece オーバーライドが確実に呼ばれる。
     */
    @SuppressWarnings("unchecked")
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, T entity,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            // innerModel / outerModel は AT で public 化済み
            // バニラの HumanoidArmorLayer と同じ: LEGS だけ innerModel、他は outerModel
            A model = (A)(slot == EquipmentSlot.LEGS ? innerModel : outerModel);
            renderArmorPiece(poseStack, bufferSource, entity, slot, packedLight, model);
        }
    }

    /**
     * AT で protected に昇格済み（accesstransformer.cfg 参照）。
     * PIXEL_DATA を持つスロットはスキップ。それ以外はバニラに委譲。
     */
    @Override
    protected void renderArmorPiece(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            T entity,
            EquipmentSlot slot,
            int packedLight,
            A model) {

        ItemStack stack = entity.getItemBySlot(slot);
        if (!stack.isEmpty() && stack.has(ModDataComponents.PATTERN_ID.get())) {
            return; // TailorArmorRenderLayer が描画するのでスキップ
        }

        super.renderArmorPiece(poseStack, bufferSource, entity, slot, packedLight, model);
    }
}
