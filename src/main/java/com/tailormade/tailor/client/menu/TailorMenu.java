package com.tailormade.tailor.client.menu;

import com.tailormade.tailor.client.gui.PatternSlot;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.entities.blockentities.TailorBlockEntity;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.registries.ModMenuTypes;
import com.tailormade.tailor.utils.DyeCostCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import static com.tailormade.tailor.utils.DesignAccessor.getPixelDataFromId;

public class TailorMenu extends AbstractContainerMenu {
    public static final int SLOT_PATTERN = 0;
    public static final int SLOT_ARMOR   = 1;
    public static final int SLOT_DYE_R   = 2;
    public static final int SLOT_DYE_G   = 3;
    public static final int SLOT_DYE_B   = 4;
    public static final int BLOCK_SLOT_COUNT = 5;

    public static final int SLOT_PATTERN_X = 72;
    public static final int SLOT_PATTERN_Y = 28;
    public static final int SLOT_ARMOR_X   = 104;
    public static final int SLOT_ARMOR_Y   = 28;
    public static final int SLOT_DYE_R_X   = 64;
    public static final int SLOT_DYE_R_Y   = 60;
    public static final int SLOT_DYE_G_X   = 64;
    public static final int SLOT_DYE_G_Y   = 76;
    public static final int SLOT_DYE_B_X   = 64;
    public static final int SLOT_DYE_B_Y   = 92;
    public static final int INV_X = 16;
    public static final int INV_Y = 134;

    private final SimpleContainer slotContainer = new SimpleContainer(BLOCK_SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            TailorMenu.this.slotsChanged(this);
        }
    };

    private final TailorBlockEntity blockEntity;

    public TailorMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        this(windowId, playerInv, (TailorBlockEntity) playerInv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public TailorMenu(int windowId, Inventory playerInv, TailorBlockEntity be) {
        super(ModMenuTypes.TAILOR_MENU.get(), windowId);
        this.blockEntity = be;
        addBlockSlots();
        addPlayerInventory(playerInv);
    }

    private void addBlockSlots() {
        addSlot(new Slot(slotContainer, SLOT_PATTERN, SLOT_PATTERN_X, SLOT_PATTERN_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (!(stack.getItem() instanceof PatternItem)) return false;
                return stack.has(ModDataComponents.PATTERN_ID.get());
            }
        });

        addSlot(new Slot(slotContainer, SLOT_ARMOR, SLOT_ARMOR_X, SLOT_ARMOR_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isCompatibleArmor(stack);
            }
        });

        addSlot(new DyeSlot(slotContainer, SLOT_DYE_R, SLOT_DYE_R_X, SLOT_DYE_R_Y, Items.RED_DYE));
        addSlot(new DyeSlot(slotContainer, SLOT_DYE_G, SLOT_DYE_G_X, SLOT_DYE_G_Y, Items.GREEN_DYE));
        addSlot(new DyeSlot(slotContainer, SLOT_DYE_B, SLOT_DYE_B_X, SLOT_DYE_B_Y, Items.BLUE_DYE));
    }

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, INV_X + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, INV_X + col * 18, INV_Y + 54));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container != slotContainer) return;

        absorbDye(SLOT_DYE_R, 'R');
        absorbDye(SLOT_DYE_G, 'G');
        absorbDye(SLOT_DYE_B, 'B');
    }

    private void absorbDye(int slotIdx, char channel) {
        ItemStack stack = slotContainer.getItem(slotIdx);
        if (stack.isEmpty()) return;

        int tank = switch (channel) {
            case 'R' -> blockEntity.getTankR();
            case 'G' -> blockEntity.getTankG();
            default -> blockEntity.getTankB();
        };

        if (tank >= TailorBlockEntity.TANK_MAX) return;

        int space = TailorBlockEntity.TANK_MAX - tank;
        int itemsNeeded = (int) Math.ceil((double) space / TailorBlockEntity.DYE_PER_ITEM);
        int itemsToUse  = Math.min(stack.getCount(), itemsNeeded);

        int amount = itemsToUse * TailorBlockEntity.DYE_PER_ITEM;
        switch (channel) {
            case 'R' -> blockEntity.addTankR(amount);
            case 'G' -> blockEntity.addTankG(amount);
            default -> blockEntity.addTankB(amount);
        }

        stack.shrink(itemsToUse);
        slotContainer.setItem(slotIdx, stack.isEmpty() ? ItemStack.EMPTY : stack);
    }

    private boolean isCompatibleArmor(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armor)) return false;

        ItemStack patternStack = slotContainer.getItem(SLOT_PATTERN);

        var pixelData = stack.get(ModDataComponents.PATTERN_ID.get());
        if (pixelData != null) {
            return false;
        }
        if (patternStack.isEmpty() || !(patternStack.getItem() instanceof PatternItem patternItem)) {
            return true;
        }
        PatternType type = patternItem.getPatternType(patternStack);
        return armor.getEquipmentSlot() == patternTypeToEquipmentSlot(type);
    }

    public static EquipmentSlot patternTypeToEquipmentSlot(PatternType type) {
        return switch (type) {
            case HEAD -> EquipmentSlot.HEAD;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case FEET -> EquipmentSlot.FEET;
        };
    }

    public boolean canConfirm() {
        ItemStack patternStack = slotContainer.getItem(SLOT_PATTERN);
        ItemStack armorStack = slotContainer.getItem(SLOT_ARMOR);

        if (patternStack.isEmpty() || armorStack.isEmpty()) return false;
        if (!(patternStack.getItem() instanceof PatternItem)) return false;

        var pixelData = getPixelDataFromId(patternStack.get(ModDataComponents.PATTERN_ID.get()));
        if (pixelData == null) return false;

        DyeCostCalculator.DyeCost cost = DyeCostCalculator.calculate(pixelData.pixels());
        return cost.canAfford(blockEntity.getTankR(), blockEntity.getTankG(), blockEntity.getTankB());
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

    public TailorBlockEntity getBlockEntity() { return blockEntity; }
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

    private static class DyeSlot extends Slot {
        private final Item allowedDye;

        DyeSlot(Container container, int index, int x, int y, Item allowedDye) {
            super(container, index, x, y);
            this.allowedDye = allowedDye;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(allowedDye);
        }
    }

    public BlockPos getBlockPos() {
        return blockEntity != null ? blockEntity.getBlockPos() : null;
    }
}