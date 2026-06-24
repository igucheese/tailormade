package com.tailormade.tailor.entities.blocks;

import com.tailormade.tailor.client.ClientHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class PowderRoomBlock extends FacingBlock {
    public PowderRoomBlock(BlockBehaviour.Properties properties) {
        super(
                properties.mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.0f)
                        .noOcclusion()
        );
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            ClientHooks.openPowderRoomScreen();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}