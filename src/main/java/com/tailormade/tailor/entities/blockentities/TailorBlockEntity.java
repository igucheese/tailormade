package com.tailormade.tailor.entities.blockentities;

import com.tailormade.tailor.client.menu.TailorMenu;
import com.tailormade.tailor.registries.ModBlockEntities;
import com.tailormade.tailor.utils.DyeCostCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class TailorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int DYE_PER_ITEM = 100;
    public static final int TANK_MAX = DYE_PER_ITEM * 32;

    private int tankR = 0;
    private int tankG = 0;
    private int tankB = 0;

    public TailorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TAILOR_BLOCK_ENTITY.get(), pos, state);
    }

    // ---- タンク操作 -------------------------------------------

    /** 指定量を R タンクに加算する。上限でクランプ。 */
    public void addTankR(int amount) { tankR = Math.min(TANK_MAX, tankR + amount); setChanged(); syncToClient(); }
    public void addTankG(int amount) { tankG = Math.min(TANK_MAX, tankG + amount); setChanged(); syncToClient(); }
    public void addTankB(int amount) { tankB = Math.min(TANK_MAX, tankB + amount); setChanged(); syncToClient(); }

    /** コストを差し引く。タンクが足りなければ false を返して何もしない。 */
    public boolean consumeDye(int costR, int costG, int costB) {
        if (tankR < costR || tankG < costG || tankB < costB) return false;
        tankR -= costR;
        tankG -= costG;
        tankB -= costB;
        setChanged();
        syncToClient();
        return true;
    }

    public int getTankR() { return tankR; }
    public int getTankG() { return tankG; }
    public int getTankB() { return tankB; }

    // ---- MenuProvider -----------------------------------------

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tailormade.tailor");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return new TailorMenu(windowId, playerInv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TankR", tankR);
        tag.putInt("TankG", tankG);
        tag.putInt("TankB", tankB);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tankR = tag.getInt("TankR");
        tankG = tag.getInt("TankG");
        tankB = tag.getInt("TankB");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
