package com.tailormade.tailor.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.registries.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tailormade.tailor.utils.DesignAccessor.getPixelDataFromId;

public class TailorTextureCompositor {

    private static final int SKIN_W = 64;
    private static final int SKIN_H = 64;

    private static final Map<UUID, TailorTextureCompositor> CACHE = new HashMap<>();

    private DynamicTexture dynamicTexture;
    private ResourceLocation textureLocation;
    private int lastHash = -1;
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    /**
     * 前回の getOrUpdate() がデータありで終わったか。
     *
     * hash == lastHash のキャッシュヒット時に
     * 「データなし」を正しく null で返すために必要。
     *
     * これがないと、装備を外した後も lastHash が一致し続け
     * 古い textureLocation を返し続けてしまう。
     */
    private boolean hasData = false;

    // ---- キャッシュ管理 ---------------------------------------

    public static TailorTextureCompositor getOrCreate(UUID uuid) {
        return CACHE.computeIfAbsent(uuid, k -> new TailorTextureCompositor());
    }

    public static void invalidate(UUID uuid) {
        TailorTextureCompositor c = CACHE.remove(uuid);
        if (c != null) c.close();
    }

    public static TailorTextureCompositor createForPreview() {
        return new TailorTextureCompositor();
    }

    // ---- 初期化 -----------------------------------------------

    private TailorTextureCompositor() {
        dynamicTexture  = new DynamicTexture(SKIN_W, SKIN_H, true);
        String textureName = "tailor_composite_" + COUNTER.getAndIncrement();
        textureLocation = Minecraft.getInstance()
                .getTextureManager()
                .register(textureName, dynamicTexture);
    }

    // ---- エンティティの装備から合成 ----------------------------

    public ResourceLocation getOrUpdate(LivingEntity entity) {
        int hash = equipmentHash(entity);

        if (hash == lastHash) {
            // キャッシュヒット: 前回の結果（データあり/なし）をそのまま返す
            return hasData ? textureLocation : null;
        }

        // ハッシュ変化 → 再評価
        Map<PatternType, int[]> pixelMap = collectPixelData(entity);
        lastHash = hash;

        if (pixelMap.isEmpty()) {
            hasData = false;
            return null;
        }

        compose(pixelMap);
        hasData = true;
        return textureLocation;
    }

    public ResourceLocation composeForPreview(Map<PatternType, int[]> pixelMap) {
        if (pixelMap.isEmpty()) return null;
        compose(pixelMap);
        return textureLocation;
    }

    // ---- 内部処理 ---------------------------------------------

    private Map<PatternType, int[]> collectPixelData(LivingEntity entity) {
        Map<PatternType, int[]> map = new EnumMap<>(PatternType.class);
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            PixelData pd = getPixelDataFromId(stack.get(ModDataComponents.PATTERN_ID.get()));
            if (pd == null) continue;
            PatternType type = equipmentSlotToPatternType(slot);
            if (type != null) map.put(type, pd.pixels());
        }
        System.out.println("[Mannequin] pixelMap.keys=" + map.keySet());
        return map;
    }

    private void compose(Map<PatternType, int[]> pixelMap) {
        NativeImage img = dynamicTexture.getPixels();
        if (img == null) return;

        for (int y = 0; y < SKIN_H; y++)
            for (int x = 0; x < SKIN_W; x++)
                img.setPixelRGBA(x, y, 0);

        for (var entry : pixelMap.entrySet()) {
            PatternType type    = entry.getKey();
            int[]       pixels  = entry.getValue();
            int         canvasW = type.getCanvasW();

            for (PatternType.CanvasSegment seg : type.getSegments()) {
                for (int y = 0; y < seg.h(); y++) {
                    for (int x = 0; x < seg.w(); x++) {
                        // キャンバス配列: (canvasY + y) * canvasW + (canvasX + x)
                        int idx = (seg.canvasY() + y) * canvasW + (seg.canvasX() + x);
                        if (idx < 0 || idx >= pixels.length) continue;

                        img.setPixelRGBA(seg.uvX() + x, seg.uvY() + y,
                                argbToAbgr(pixels[idx]));
                    }
                }
            }
        }

        dynamicTexture.upload();
    }

    private static int argbToAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b =  argb        & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private static int equipmentHash(LivingEntity entity) {
        int hash = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            hash = hash * 31 + entity.getItemBySlot(slot).hashCode();
        }
        return hash;
    }

    private static PatternType equipmentSlotToPatternType(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD  -> PatternType.HEAD;
            case CHEST -> PatternType.CHEST;
            case LEGS  -> PatternType.LEGS;
            case FEET  -> PatternType.FEET;
            default    -> null;
        };
    }

    public void close() {
        if (dynamicTexture != null) {
            dynamicTexture.close();
            dynamicTexture = null;
        }
    }
}