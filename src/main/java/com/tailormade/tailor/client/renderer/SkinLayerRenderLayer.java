package com.tailormade.tailor.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tailormade.tailor.data.*;
import com.tailormade.tailor.registries.ModDataComponents;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkinLayerRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final Map<UUID, TailorTextureCompositor> SKIN_CACHE = new HashMap<>();
    private static final Map<UUID, int[]> SKIN_PIXEL_CACHE = new HashMap<>();
    private static final Map<UUID, UnderwearSetting> UNDERWEAR_CACHE = new HashMap<>();

    private static ResourceLocation skinPreviewOverride = null;
    private static UnderwearSetting underwearPreviewOverride = null;

    public static void setSkinPreview(ResourceLocation tex) { skinPreviewOverride = tex; }
    public static void setUnderwearPreview(UnderwearSetting s) { underwearPreviewOverride = s; }
    public static void clearPreview() {
        skinPreviewOverride = null;
        underwearPreviewOverride = null;
    }

    public static void invalidateSkin(UUID uuid) {
        TailorTextureCompositor c = SKIN_CACHE.remove(uuid);
        if (c != null) c.close();
    }

    public static void updateSkinPixels(UUID uuid, int[] pixels) {
        SKIN_PIXEL_CACHE.put(uuid, pixels);
        invalidateSkin(uuid);
    }

    public static void updateUnderwear(UUID uuid, UnderwearSetting setting) {
        UNDERWEAR_CACHE.put(uuid, setting);
    }

    public static int[] getSkinPixels(UUID uuid) {
        PixelData data = SkinDataClientCache.get(uuid);
        if (data == null) {
            return null;
        }
        return data.getPixels();
//        return SKIN_PIXEL_CACHE.get(uuid);
    }
    public static UnderwearSetting getUnderwearSetting(UUID uuid) {
        UnderwearSetting setting = UnderwearDataClientCache.get(uuid);
        if (setting == null) {
            return UnderwearSetting.DEFAULT;
        }
        return setting;
//        return UNDERWEAR_CACHE.getOrDefault(uuid, UnderwearSetting.DEFAULT);
    }

    public SkinLayerRenderLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        if (!hasTailorArmor(player)) return;

        ResourceLocation skinTex = resolveSkinTexture(player);
        if (skinTex != null) {
            getParentModel().renderToBuffer(
                    poseStack,
                    bufferSource.getBuffer(RenderType.entityTranslucentCull(skinTex)),
                    packedLight,
                    OverlayTexture.NO_OVERLAY
            );
        }

        UnderwearSetting underwear = resolveUnderwear(player);
        if (underwear != null) {
            ResourceLocation underwearTex = underwear.type().getTexture();
            getParentModel().renderToBuffer(
                    poseStack,
                    bufferSource.getBuffer(RenderType.entityTranslucentCull(underwearTex)),
                    packedLight,
                    OverlayTexture.NO_OVERLAY
            );
        }
    }

    private ResourceLocation resolveSkinTexture(AbstractClientPlayer player) {
        if (skinPreviewOverride != null) return skinPreviewOverride;

        UUID  uuid   = player.getUUID();
        PixelData data = SkinDataClientCache.get(uuid);
//        int[] pixels = SKIN_PIXEL_CACHE.get(uuid);
        int[] pixels = data != null ? data.getPixels() : null;

        if (pixels == null || pixels.length != 64 * 64) return null;

        TailorTextureCompositor compositor =
                SKIN_CACHE.computeIfAbsent(uuid, k -> TailorTextureCompositor.createForPreview());

        Map<PatternType, int[]> pixelMap = buildSkinPixelMap(player, pixels);
        if (pixelMap.isEmpty()) return null;

        return compositor.composeForPreview(pixelMap);
    }

    private Map<PatternType, int[]> buildSkinPixelMap(
            AbstractClientPlayer player, int[] full64x64) {

        Map<PatternType, int[]> map = new EnumMap<>(PatternType.class);

        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {

            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.has(ModDataComponents.PATTERN_ID.get())) continue;

            PatternType type = slotToPatternType(slot);
            if (type == null) continue;

            int[] canvas = new int[type.getCanvasW() * type.getCanvasH()];
            for (PatternType.CanvasSegment seg : type.getSegments()) {
                for (int y = 0; y < seg.h(); y++) {
                    for (int x = 0; x < seg.w(); x++) {
                        int srcIdx  = (seg.uvY() + y) * 64 + (seg.uvX() + x);
                        int dstIdx = (seg.canvasY() + y) * type.getCanvasW() + (seg.canvasX() + x);
                        if (srcIdx < full64x64.length && dstIdx < canvas.length) {
                            canvas[dstIdx] = full64x64[srcIdx];
                        }
                    }
                }
            }
            map.put(type, canvas);
        }
        return map;
    }

    private UnderwearSetting resolveUnderwear(AbstractClientPlayer player) {
        if (underwearPreviewOverride != null) return underwearPreviewOverride;
        return UnderwearDataClientCache.get(player.getUUID());
//        return UNDERWEAR_CACHE.get(player.getUUID());
    }

    private boolean hasTailorArmor(AbstractClientPlayer player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.has(ModDataComponents.PATTERN_ID.get())) return true;
        }
        return false;
    }

    private PatternType slotToPatternType(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD  -> PatternType.HEAD;
            case CHEST -> PatternType.CHEST;
            case LEGS  -> PatternType.LEGS;
            case FEET  -> PatternType.FEET;
            default    -> null;
        };
    }
}
