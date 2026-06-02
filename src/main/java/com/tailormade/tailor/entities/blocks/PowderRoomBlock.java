package com.tailormade.tailor.entities.blocks;

import com.tailormade.tailor.client.menu.DesignerMenu;
import com.tailormade.tailor.client.screen.PowderRoomScreen;
import com.tailormade.tailor.entities.blockentities.TailorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

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
            Minecraft.getInstance().setScreen(new PowderRoomScreen());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}