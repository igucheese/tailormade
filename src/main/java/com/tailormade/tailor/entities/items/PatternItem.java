package com.tailormade.tailor.entities.items;

import com.tailormade.tailor.data.*;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.registries.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.tailormade.tailor.data.PatternType.*;
import static com.tailormade.tailor.utils.DesignAccessor.getDesignDataFromId;
import static com.tailormade.tailor.utils.DesignAccessor.getPixelDataFromId;

public class PatternItem extends Item {
    private final PatternType patternType;

    public PatternItem(PatternType patternType, Properties properties) {
        super(properties);
        this.patternType = patternType;
    }

    public PatternType getPatternType(ItemStack mainStack) {
        return this.patternType;
    }

    @Nullable
    public int[] getPixelData(ItemStack stack) {
        PixelData data = getPixelDataFromId(stack.get(ModDataComponents.PATTERN_ID.get()));
        return data != null ? data.pixels() : null;
    }

    public DesignDataRecord getDesignData(ItemStack stack) {
        return getDesignDataFromId(stack.get(ModDataComponents.PATTERN_ID.get()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String patternId = stack.get(ModDataComponents.PATTERN_ID.get());
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        String typeName = "";
        if (stack.is(ModItems.PATTERN_HELMET.get())) {
            typeName = "helmet";
        } else if (stack.is(ModItems.PATTERN_CHESTPLATE.get())) {
            typeName = "chestplate";
        } else if (stack.is(ModItems.PATTERN_LEGGINGS.get())) {
            typeName = "leggings";
        } else if (stack.is(ModItems.PATTERN_BOOTS.get())) {
            typeName = "boots";
        }
        if (stack.has(ModDataComponents.IS_COPIED) && Boolean.TRUE.equals(stack.get(ModDataComponents.IS_COPIED.get()))) {
            tooltip.add(Component.translatable("item.tailormade.pattern.copied").withStyle(ChatFormatting.GRAY));
        }
        if (stack.has(ModDataComponents.IS_EXTRACTED) && Boolean.TRUE.equals(stack.get(ModDataComponents.IS_EXTRACTED.get()))) {
            tooltip.add(Component.translatable("item.tailormade.pattern.extracted").withStyle(ChatFormatting.GRAY));
        }
        if (stack.has(ModDataComponents.IS_IMPORTED) && Boolean.TRUE.equals(stack.get(ModDataComponents.IS_IMPORTED.get()))) {
            tooltip.add(Component.translatable("item.tailormade.pattern.imported").withStyle(ChatFormatting.GRAY));
        }
        if (!typeName.isBlank() && customName != null) {
            tooltip.add(Component.translatable("item.tailormade.pattern.description.type." + typeName).withStyle(ChatFormatting.GRAY));
        }
        if (patternId != null) {
            DesignDataRecord dataRecord = DesignDataClientCache.get(UUID.fromString(patternId));
            if (typeName.equals("chestplate")) {
                String slimSuffix = dataRecord.isSlim() ? "slim" : "regular";
                tooltip.add(Component.translatable("item.tailormade.pattern.description." + slimSuffix).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
            if (dataRecord.isLocked()) {
                tooltip.add(Component.translatable("item.tailormade.pattern.description.locked").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
            if (dataRecord.designerId() != null) {
                UUID searchingId = (!dataRecord.designerId().equals(dataRecord.userId())) ? dataRecord.designerId() : dataRecord.userId();
                GlobalPlayer playerData = GlobalPlayerCache.get(searchingId);
                String playerName = null;
                if (playerData != null) {
                    playerName = playerData.name();
                } else {
                    playerName = dataRecord.designerId().toString();
                }
                tooltip.add(Component.translatable("item.tailormade.pattern.description.designer", playerName).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    }
}
