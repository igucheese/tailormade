package com.tailormade.tailor.client.gui;

import com.tailormade.tailor.entities.items.PatternItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PatternSlot extends Slot {
    public PatternSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof PatternItem;
    }
    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
