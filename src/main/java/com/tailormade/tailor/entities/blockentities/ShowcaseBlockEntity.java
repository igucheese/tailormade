package com.tailormade.tailor.entities.blockentities;

import com.tailormade.tailor.registries.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class ShowcaseBlockEntity extends BlockEntity {
    private UUID catalogId;
    private UUID ownerId;

    public ShowcaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHOWCASE_BLOCK_ENTITY.get(), pos, state);
    }

    public void setCatalogId(UUID id) {
        this.catalogId = id;
        setChanged();
    }
    public UUID getCatalogId() { return this.catalogId; }

    public void setOwnerId(UUID id) {
        this.ownerId = id;
        setChanged();
    }
    public UUID getOwnerId() { return this.ownerId; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("catalogId", catalogId);
        tag.putUUID("ownerId", ownerId);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        catalogId = tag.getUUID("catalogId");
        ownerId = tag.getUUID("ownerId");
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
