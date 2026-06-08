package com.tailormade.tailor.client.menu;

import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.entities.blockentities.TailorBlockEntity;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.registries.ModItems;
import com.tailormade.tailor.registries.ModMenuTypes;
import com.tailormade.tailor.utils.DyeCostCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class BleachMenu extends AbstractContainerMenu {
    public static final int SLOT_ARMOR = 0;
    public static final int SLOT_BLEACH = 1;
    public static final int BLOCK_SLOT_COUNT = 2;

    public static final int SLOT_ARMOR_X   = 31;
    public static final int SLOT_ARMOR_Y   = 12;
    public static final int SLOT_BLEACH_X   = 63;
    public static final int SLOT_BLEACH_Y   = 12;
    public static final int INV_1_X = 11;
    public static final int INV_1_Y = 40;
    public static final int INV_2_X = 20;
    public static final int INV_2_Y = 58;

    private final SimpleContainer slotContainer = new SimpleContainer(BLOCK_SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            BleachMenu.this.slotsChanged(this);
        }
    };

    public BleachMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        this(windowId, playerInv);
    }

    public BleachMenu(int windowId, Inventory playerInv) {
        super(ModMenuTypes.BLEACH_MENU.get(), windowId);
        addBlockSlots();
        addPlayerInventory(playerInv);
    }

    private void addBlockSlots() {
        addSlot(new Slot(slotContainer, SLOT_ARMOR, SLOT_ARMOR_X, SLOT_ARMOR_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.has(ModDataComponents.PATTERN_ID.get()) && stack.getItem() instanceof ArmorItem;
            }
        });
        addSlot(new Slot(slotContainer, SLOT_BLEACH, SLOT_BLEACH_X, SLOT_BLEACH_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.BLEACH.get());
            }
        });
    }

    private void addPlayerInventory(Inventory inv) {
        for (int col = 0; col < 9; col++) {
            int baseX, baseY;
            if (col < 5) {
                baseX = INV_1_X;
                baseY = INV_1_Y;
            } else {
                baseX = INV_2_X - 90;
                baseY = INV_2_Y;
            }
            addSlot(new Slot(inv, col, baseX + col * 18, baseY));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
    }

    public boolean canBleach() {
        ItemStack armorStack = slotContainer.getItem(SLOT_ARMOR);

        if (armorStack.isEmpty()) return false;
        var pixelData = armorStack.get(ModDataComponents.PATTERN_ID.get());
        if (!(armorStack.getItem() instanceof ArmorItem armor)) return false;
        if (pixelData == null) return false;

        ItemStack bleachStack = slotContainer.getItem(SLOT_BLEACH);

        return bleachStack.is(ModItems.BLEACH.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return result;

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < BLOCK_SLOT_COUNT) {
            if (!moveItemStackTo(stack, BLOCK_SLOT_COUNT, slots.size(), true))
                return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, BLOCK_SLOT_COUNT, false))
                return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public SimpleContainer getSlotContainer()  { return slotContainer; }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            for (int i = 0; i < BLOCK_SLOT_COUNT; i++) {
                ItemStack itemStack = this.getSlot(i).getItem();
                if (!itemStack.isEmpty()) {
                    this.clearContainer(player, this.slotContainer);
                }
            }
        }
    }
}