package com.tailormade.tailor.client.menu;

import com.tailormade.tailor.client.gui.PatternSlot;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.registries.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DesignerMenu extends AbstractContainerMenu {
    public static final int MAIN_SLOT = 0;
    public static final int PREVIEW_SLOT1 = 1;
    public static final int PREVIEW_SLOT2 = 2;
    public static final int PREVIEW_SLOT3 = 3;
    public static final int SLOT_COUNT = 4;
    private final Player player;

    private final SimpleContainer patternContainer = new SimpleContainer(SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            DesignerMenu.this.slotsChanged(this);
        }
    };

    public DesignerMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        this(windowId, playerInv);
    }
    public DesignerMenu(int windowId, Inventory playerInv) {
        super(ModMenuTypes.DESIGNER_MENU.get(), windowId);
        addPatternSlots();
        addPlayerInventory(playerInv);
        this.player = playerInv.player;
    }

    private void addPatternSlots() {
        addSlot(new PatternSlot(patternContainer, MAIN_SLOT, 8, 7, this.player));
        addSlot(new PreviewPatternSlot(patternContainer, PREVIEW_SLOT1, 248, 157));
        addSlot(new PreviewPatternSlot(patternContainer, PREVIEW_SLOT2, 266, 157));
        addSlot(new PreviewPatternSlot(patternContainer, PREVIEW_SLOT3, 284, 157));
    }

    private void addPlayerInventory(Inventory inv) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 141 + col * 18, 204) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof PatternItem;
                }
            });
        }
    }

    public boolean canPlaceInPreviewSlot(ItemStack stack) {
        if (!(stack.getItem() instanceof PatternItem incoming)) return false;
        ItemStack mainStack = patternContainer.getItem(MAIN_SLOT);
        if (mainStack.isEmpty()) return true;
        if (!(mainStack.getItem() instanceof PatternItem main)) return true;

        PatternType incomingType = incoming.getPatternType(stack);
        PatternType mainType = main.getPatternType(mainStack);
        return incomingType != mainType;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public SimpleContainer getPatternContainer() {
        return patternContainer;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;

        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < SLOT_COUNT) {
                if (!this.moveItemStackTo(itemstack1, SLOT_COUNT, SLOT_COUNT + 9, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack1.getItem() instanceof PatternItem) {
                if (!this.moveItemStackTo(itemstack1, 0, SLOT_COUNT - 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            for (int i = 0; i < SLOT_COUNT; i++) {
                ItemStack itemStack = this.getSlot(i).getItem();
                if (!itemStack.isEmpty()) {
                    this.clearContainer(player, this.patternContainer);
                }
            }
        }
    }

    private class PreviewPatternSlot extends PatternSlot {
        public PreviewPatternSlot(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y, null);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return super.mayPlace(stack) && canPlaceInPreviewSlot(stack);
        }
    }
}