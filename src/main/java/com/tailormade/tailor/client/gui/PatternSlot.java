package com.tailormade.tailor.client.gui;

import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.utils.DesignGuard;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class PatternSlot extends Slot {
    private final Player player;
    public PatternSlot(Container container, int index, int x, int y, Player player) {
        super(container, index, x, y);
        this.player = player;
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        // ロックされていて、かつ自分のものではない型紙は置けないようにする
        if (stack.getItem() instanceof PatternItem) {
            if (!stack.has(ModDataComponents.PATTERN_ID.get())) {
                return stack.getItem() instanceof PatternItem;
            }
            String patternIdStr = stack.get(ModDataComponents.PATTERN_ID.get());
            if (patternIdStr != null && this.player != null) {
                return DesignGuard.canEdit(UUID.fromString(patternIdStr), this.player.getUUID(), true);
            }
        }
        return stack.getItem() instanceof PatternItem;
    }
    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
