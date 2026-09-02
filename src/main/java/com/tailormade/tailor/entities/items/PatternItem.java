package com.tailormade.tailor.entities.items;

import com.tailormade.tailor.data.DesignDataClientCache;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.data.PixelData;
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
        if (!typeName.isBlank() && customName != null) {
            tooltip.add(Component.translatable("item.tailormade.pattern.description.type." + typeName).withStyle(ChatFormatting.GRAY));
        }
        if (patternId != null) {
            tooltip.add(Component.translatable("item.tailormade.pattern.description.edited").withStyle(ChatFormatting.GRAY));
            DesignDataRecord dataRecord = DesignDataClientCache.get(UUID.fromString(patternId));
            if (dataRecord.isLocked()) {
                tooltip.add(Component.translatable("item.tailormade.pattern.description.locked").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    }
}
