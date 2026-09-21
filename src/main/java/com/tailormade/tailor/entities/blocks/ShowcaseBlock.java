package com.tailormade.tailor.entities.blocks;

import com.tailormade.tailor.client.ClientHooks;
import com.tailormade.tailor.entities.blockentities.MannequinEntity;
import com.tailormade.tailor.entities.blockentities.ShowcaseBlockEntity;
import com.tailormade.tailor.entities.blockentities.TailorBlockEntity;
import com.tailormade.tailor.network.payloads.PrepareCatalogScreenPayload;
import com.tailormade.tailor.registries.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ShowcaseBlock extends FacingBlock implements EntityBlock {
    public ShowcaseBlock(Properties properties) {
        super(
                properties.mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.0f)
                        .noOcclusion()
        );
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ShowcaseBlockEntity sbe) {
                UUID catalogId = sbe.getCatalogId();
                PacketDistributor.sendToServer(new PrepareCatalogScreenPayload(catalogId, false, pos));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ShowcaseBlockEntity(blockPos, blockState);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ShowcaseBlockEntity sbe) {
                sbe.setOwnerId(placer.getUUID());

                if (sbe.getCatalogId() == null) {
                    sbe.setCatalogId(UUID.randomUUID());
                }
            }
        }
    }
}