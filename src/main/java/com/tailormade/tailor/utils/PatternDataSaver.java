package com.tailormade.tailor.utils;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.*;
import com.tailormade.tailor.data.records.LayerData;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.registries.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class PatternDataSaver {
    public static boolean isValidSize(ItemStack stack, PixelData pixelData, PatternItem patternItem) {
        int[] incomingPixels = pixelData.pixels();
        int[] expectedSize = patternItem.getPatternType(stack).getTextureSize();
        int expectedLen = expectedSize[0] * expectedSize[1];
        if (incomingPixels.length != expectedLen) {
            Tailormade.LOGGER.warn(
                    "SavePattern: ピクセルデータのサイズが不正です (期待値={}, 実際の値={})",
                    expectedLen, incomingPixels.length
            );
            return false;
        }
        return true;
    }

    public static DesignDataRecord saveDeign(ServerLevel level, ItemStack stack, PixelData pixelData, List<LayerData> layers, PatternItem patternItem, UUID designer, String patternName, boolean isSlim) {
        return saveDeign(level, stack, pixelData, layers, patternItem, designer, patternName, designer, isSlim);
    }

    public static DesignDataRecord saveDeign(ServerLevel level, ItemStack stack, PixelData pixelData, List<LayerData> layers, PatternItem patternItem, UUID designer, String patternName, UUID originalDesigner, boolean isSlim) {
        return saveDeign(level, stack, pixelData, layers, patternItem, designer, patternName, originalDesigner, false, false, false, isSlim);
    }

    public static DesignDataRecord saveDeign(ServerLevel level, ItemStack stack, PixelData pixelData, List<LayerData> layers, PatternItem patternItem, UUID designer, String patternName, UUID originalDesigner, boolean isCopied, boolean isExtracted, boolean isImported, boolean isSlim) {
        try {
            int[] incomingPixels = pixelData.pixels();

            String existingUuid = stack.get(ModDataComponents.PATTERN_ID.get());
            UUID uuid;
            if (existingUuid == null || existingUuid.isBlank()) {
                uuid = UUID.randomUUID();
            } else {
                uuid = UUID.fromString(existingUuid);
            }
            PatternType type = patternItem.getPatternType(stack);
            DesignDataRecord newDesign = new DesignDataRecord(
                    uuid,
                    new PixelData(incomingPixels),
                    layers,
                    designer,
                    patternName,
                    type.getType(),
                    false,
                    originalDesigner,
                    isSlim
            );
            DesignData.get(level).addDesign(newDesign);

            stack.set(ModDataComponents.PATTERN_ID.get(), newDesign.uuid().toString());
            if (!patternName.isBlank()) {
                stack.set(ModDataComponents.PATTERN_NAME.get(), patternName);
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(patternName));
            }
            if (isCopied) {
                stack.set(ModDataComponents.IS_COPIED.get(), true);
            }
            if (isExtracted) {
                stack.set(ModDataComponents.IS_EXTRACTED.get(), true);
            }
            if (isImported) {
                stack.set(ModDataComponents.IS_IMPORTED.get(), true);
            }

            return newDesign;
        } catch (Exception e) {
            Tailormade.LOGGER.error("[PatternDataSaver.saveDesign] エラーが発生しました: {}", e.getMessage());
            return null;
        }
    }
}
