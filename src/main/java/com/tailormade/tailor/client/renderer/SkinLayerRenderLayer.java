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

import java.util.*;

public class SkinLayerRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final Map<UUID, TailorTextureCompositor> SKIN_CACHE = new HashMap<>();
    private static final Map<UUID, int[]> SKIN_PIXEL_CACHE = new HashMap<>();
    private static final Map<UUID, UnderwearSetting> UNDERWEAR_CACHE = new HashMap<>();
    private static final Map<UUID, UnderwearTextureCompositor> UNDERWEAR_COMPOSITOR_CACHE = new HashMap<>();
    private static final Map<UUID, UnderwearType> UNDERWEAR_TYPE_CACHE = new HashMap<>();

    private static ResourceLocation skinPreviewOverride = null;
    private static UnderwearSetting underwearPreviewOverride = null;
    private static ResourceLocation underwearPreviewTexture = null;
    private static boolean isForcePreview = false;

    public static void setSkinPreview(ResourceLocation tex) { skinPreviewOverride = tex; }
    public static void setUnderwearPreview(UnderwearSetting s) {
        underwearPreviewOverride = s;
        underwearPreviewTexture = null;
    }
    public static void setUnderwearPreview(UnderwearSetting s, ResourceLocation texture) {
        underwearPreviewOverride = s;
        underwearPreviewTexture = texture;
    }
    public static void clearPreview() {
        skinPreviewOverride = null;
        underwearPreviewOverride = null;
        underwearPreviewTexture = null;
    }
    public static void setIsForcePreview(boolean isForced) {
        isForcePreview = isForced;
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
    }
    public static UnderwearSetting getUnderwearSetting(UUID uuid) {
        UnderwearSetting setting = UnderwearDataClientCache.get(uuid);
        if (setting == null) {
            return UnderwearSetting.DEFAULT;
        }
        return setting;
    }

    public SkinLayerRenderLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!isForcePreview && !hasTailorArmor(player)) return;

        ResourceLocation skinTex = resolveSkinTexture(player);
        if (skinTex != null) {
            getParentModel().renderToBuffer(
                    poseStack,
                    bufferSource.getBuffer(RenderType.entityTranslucentCull(skinTex)),
                    packedLight,
                    OverlayTexture.NO_OVERLAY
            );
        }

        ResourceLocation underwearTex = resolveUnderwearTexture(player);
        if (underwearTex != null) {
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

        UUID playerId = player.getUUID();
        PixelData data = SkinDataClientCache.get(playerId);
        int[] pixels = data != null ? data.getPixels() : null;

        if (pixels == null || pixels.length != 64 * 64) {
            return null;
        }
        TailorTextureCompositor compositor = SKIN_CACHE.computeIfAbsent(playerId, k -> TailorTextureCompositor.createForPreview(playerId));

        Map<PatternType, int[]> pixelMap = buildSkinPixelMap(player, pixels);
        if (pixelMap.isEmpty()) return null;

        return compositor.composeForPreview(pixelMap);
    }

    private Map<PatternType, int[]> buildSkinPixelMap(AbstractClientPlayer player, int[] full64x64) {
        Map<PatternType, int[]> map = new EnumMap<>(PatternType.class);
        for (EquipmentSlot slot : new EquipmentSlot[]{ EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!isForcePreview && (stack.isEmpty() || !stack.has(ModDataComponents.PATTERN_ID.get()))) continue;

            PatternType type = slotToPatternType(slot);
            if (!isForcePreview && (type == null)) continue;

            int[] canvas = new int[type.getCanvasW() * type.getCanvasH()];
            for (PatternType.CanvasSegment seg : type.getSegments()) {
                for (int y = 0; y < seg.h(); y++) {
                    for (int x = 0; x < seg.w(); x++) {
                        int srcIdx = (seg.uvY() + y) * 64 + (seg.uvX() + x);
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
    }

    private ResourceLocation resolveUnderwearTexture(AbstractClientPlayer player) {
        Set<EquipmentSlot> activeSlots = isForcePreview ? Set.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET) : getActiveTailorSlots(player);
        if (activeSlots.isEmpty()) return null;

        if (underwearPreviewOverride != null) {
            if (underwearPreviewTexture != null) return underwearPreviewTexture;
            return composeUnderwearTexture(
                    player.getUUID(),
                    underwearPreviewOverride,
                    activeSlots
            );
        }
        UnderwearSetting setting = UnderwearDataClientCache.get(player.getUUID());
        if (setting == null) return null;
        return composeUnderwearTexture(player.getUUID(), setting, activeSlots);
    }

    private Set<EquipmentSlot> getActiveTailorSlots(AbstractClientPlayer player) {
        Set<EquipmentSlot> slots = new HashSet<>();
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.has(ModDataComponents.PATTERN_ID.get())) {
                slots.add(slot);
            }
        }
        return slots;
    }

    private static ResourceLocation composeUnderwearTexture(UUID playerId, UnderwearSetting setting, Set<EquipmentSlot> activeSlots) {
        UnderwearType currentType = UNDERWEAR_TYPE_CACHE.get(playerId);

        if (currentType != setting.type()) {
            UnderwearTextureCompositor old = UNDERWEAR_COMPOSITOR_CACHE.remove(playerId);
            if (old != null) old.close();
            UNDERWEAR_TYPE_CACHE.put(playerId, setting.type());
        }

        UnderwearTextureCompositor compositor = UNDERWEAR_COMPOSITOR_CACHE.computeIfAbsent(
                playerId, k -> {
                    UnderwearTextureCompositor c = new UnderwearTextureCompositor();
                    c.init(setting.type().getTexture(), playerId);
                    return c;
                }
        );
        return compositor.compose(setting.color(), activeSlots);
    }

    private boolean hasTailorArmor(AbstractClientPlayer player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{ EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.has(ModDataComponents.PATTERN_ID.get())) return true;
        }
        return false;
    }

    private PatternType slotToPatternType(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> PatternType.HEAD;
            case CHEST -> PatternType.CHEST;
            case LEGS -> PatternType.LEGS;
            case FEET -> PatternType.FEET;
            default -> null;
        };
    }
}
