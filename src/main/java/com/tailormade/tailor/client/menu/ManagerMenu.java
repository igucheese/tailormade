package com.tailormade.tailor.client.menu;

import com.tailormade.tailor.data.DesignDataClientCache;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.entities.items.PatternItem;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.registries.ModItems;
import com.tailormade.tailor.registries.ModMenuTypes;
import com.tailormade.tailor.utils.DesignGuard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ManagerMenu extends AbstractContainerMenu {
    public static final int SLOT_PATTERN = 0;
    public static final int SLOT_COPY_PATTERN = 1;
    public static final int SLOT_TAILORED_ARMOUR = 2;
    public static final int SLOT_EXTRACT_PATTERN = 3;
    public static final int BLOCK_SLOT_COUNT = 4;

    public static final int SLOT_PATTERN_X = 32;
    public static final int SLOT_PATTERN_Y = 64;
    public static final int SLOT_COPY_PATTERN_X = 104;
    public static final int SLOT_COPY_PATTERN_Y = 52;
    public static final int SLOT_EXTRACT_PATTERN_X = 136;
    public static final int SLOT_EXTRACT_PATTERN_Y = 72;
    public static final int INV_X = 32;
    public static final int INV_Y = 144;

    private final Player player;

    // タブ切り替え用データ
    private final ContainerData tabData = new SimpleContainerData(1); // 現在のタブ ID を入れるだけ用
    public static final int TAB_PATTERN = 0;
    public static final int TAB_EXTRACT = 1;
    public static final int TAB_IMPORT = 2;

    private final SimpleContainer slotContainer = new SimpleContainer(BLOCK_SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            ManagerMenu.this.slotsChanged(this);
        }
    };
    public SimpleContainer getPatternContainer() {
        return slotContainer;
    }

    public ManagerMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        this(windowId, playerInv);
    }

    public ManagerMenu(int windowId, Inventory playerInv) {
        super(ModMenuTypes.MANAGER_MENU.get(), windowId);
        addBlockSlots();
        addPlayerInventory(playerInv);
        this.addDataSlots(tabData);
        this.player = playerInv.player;
    }

    private void addBlockSlots() {
        // TabOnlySlot(Container container, int index, int x, int y, int tab, SlotType type, ContainerData tabData)
        // 管理対象型紙（データ記録済みのもののみ）
        addSlot(new TabOnlySlot(slotContainer, SLOT_PATTERN, SLOT_PATTERN_X, SLOT_PATTERN_Y, TAB_PATTERN, tabData) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.has(ModDataComponents.PATTERN_ID.get()) && stack.getItem() instanceof PatternItem;
            }
        });
//        addSlot(new Slot(slotContainer, SLOT_PATTERN, SLOT_PATTERN_X, SLOT_PATTERN_Y) {
//            @Override
//            public boolean mayPlace(ItemStack stack) {
//                return stack.has(ModDataComponents.PATTERN_ID.get()) && stack.getItem() instanceof PatternItem;
//            }
//        });
        // オリジナルをコピーする先の型紙
//        addSlot(new Slot(slotContainer, SLOT_COPY_PATTERN, SLOT_COPY_PATTERN_X, SLOT_COPY_PATTERN_Y) {
        addSlot(new TabOnlySlot(slotContainer, SLOT_COPY_PATTERN, SLOT_COPY_PATTERN_X, SLOT_COPY_PATTERN_Y, TAB_PATTERN, tabData) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                ItemStack orgStack = slotContainer.getItem(SLOT_PATTERN);
                if (orgStack.isEmpty()) return false;
                if (!(orgStack.getItem() instanceof PatternItem pattern)) return false;
                if (!(stack.getItem() instanceof PatternItem copyPattern)) return false;
                String orgType = pattern.getPatternType(orgStack).getType();
                String copyType = copyPattern.getPatternType(stack).getType();
                if (!orgType.equals(copyType)) return false;
                return !stack.has(ModDataComponents.PATTERN_ID.get());
            }
        });

        // 型紙を抽出したい防具
        addSlot(new TabOnlySlot(slotContainer, SLOT_TAILORED_ARMOUR, SLOT_PATTERN_X, SLOT_PATTERN_Y, TAB_EXTRACT, tabData) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.has(ModDataComponents.PATTERN_ID.get()) && stack.getItem() instanceof ArmorItem;
            }
        });
        // 抽出先の白紙型紙
        addSlot(new TabOnlySlot(slotContainer, SLOT_EXTRACT_PATTERN, SLOT_EXTRACT_PATTERN_X, SLOT_EXTRACT_PATTERN_Y, TAB_EXTRACT, tabData) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                ItemStack orgStack = slotContainer.getItem(SLOT_TAILORED_ARMOUR);
                if (orgStack.isEmpty()) return false;
                if (!isCompatiblePattern(stack)) { return false; }
                return !stack.has(ModDataComponents.PATTERN_ID.get());
            }
        });
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

    public void setTab(int tab) {
        tabData.set(0, tab);
        System.out.println("[CHECK][ManagerMenu.setTab] " + tab);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
    }

    public UUID getCurrentOriginalPatternId() {
        return getPatternIdFromSlot(SLOT_PATTERN);
    }
    public UUID getCurrentExtractPatternId() {
        return getPatternIdFromSlot(SLOT_TAILORED_ARMOUR);
    }

    public UUID getPatternIdFromSlot(int slot) {
        ItemStack orgStack = slotContainer.getItem(slot);
        if (orgStack.isEmpty()) return null;
        if (!orgStack.has(ModDataComponents.PATTERN_ID.get())) return null;
        String patternId = orgStack.get(ModDataComponents.PATTERN_ID.get());
        if (patternId == null) return null;
        return UUID.fromString(patternId);
    }

    public boolean isPatternSet() {
        ItemStack orgStack = slotContainer.getItem(SLOT_PATTERN);
        if (orgStack.isEmpty() || !(orgStack.getItem() instanceof PatternItem)) return false;
        return true;
    }

    public boolean isLocked() {
        ItemStack orgStack = slotContainer.getItem(SLOT_PATTERN);
        if (orgStack.isEmpty()) return false;
        if (!orgStack.has(ModDataComponents.PATTERN_ID.get())) return false;
        String patternIdStr = orgStack.get(ModDataComponents.PATTERN_ID.get());
        if (patternIdStr == null) return false;
        UUID patternId = UUID.fromString(patternIdStr);
        DesignDataRecord designData = DesignDataClientCache.get(patternId);
        if (designData == null) return false;
        return designData.isLocked();
    }

    public boolean canCopy() {
        ItemStack orgStack = slotContainer.getItem(SLOT_PATTERN);

        if (orgStack.isEmpty()) return false;
        String patternId = orgStack.get(ModDataComponents.PATTERN_ID.get());
        if (!(orgStack.getItem() instanceof PatternItem pattern)) return false;
        if (patternId == null) return false;

        ItemStack copyStack = slotContainer.getItem(SLOT_COPY_PATTERN);
        if (copyStack.isEmpty()) return false;

        // ロックされていて編集できない場合は一律アウト
        if (!DesignGuard.canEdit(UUID.fromString(patternId), player.getUUID())) {
            return false;
        }

        return (copyStack.getItem() instanceof PatternItem) && !copyStack.has(ModDataComponents.PATTERN_ID.get());
    }

    private boolean isCompatiblePattern(ItemStack stack) {
        if (!(stack.getItem() instanceof PatternItem pattern)) return false;

        ItemStack armorStack = slotContainer.getItem(SLOT_TAILORED_ARMOUR);
        if (!(armorStack.getItem() instanceof ArmorItem armor)) return false;

        String patternId = stack.get(ModDataComponents.PATTERN_ID.get());
        if (patternId != null) { return false; }
        PatternType type = pattern.getPatternType(stack);
        return armor.getEquipmentSlot() == TailorMenu.patternTypeToEquipmentSlot(type);
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
            int startIndex = 0;
            int endIndex = BLOCK_SLOT_COUNT;
            if (this.tabData.get(0) == 0) {
                endIndex = 2;
            }
            if (this.tabData.get(0) == 1) {
                startIndex = 2;
            }
            if (!moveItemStackTo(stack, startIndex, endIndex, false))
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

    private static class TabOnlySlot extends Slot {
        private final ContainerData tabData;
        private final int tabToShow;

        public TabOnlySlot(Container container, int index, int x, int y, int tab, ContainerData tabData) {
            super(container, index, x, y);
            this.tabData = tabData;
            this.tabToShow = tab;
        }

        @Override
        public boolean isActive() {
            return tabData.get(0) == tabToShow;
        }
    }
}