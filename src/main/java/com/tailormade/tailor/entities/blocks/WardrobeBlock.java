package com.tailormade.tailor.entities.blocks;

import com.tailormade.tailor.client.screen.PowderRoomScreen;
import com.tailormade.tailor.client.screen.WardrobeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class WardrobeBlock extends FacingBlock {
    public WardrobeBlock(Properties properties) {
        super(
                properties.mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.0f)
                        .noOcclusion()
        );
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            Minecraft.getInstance().setScreen(new WardrobeScreen());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}