package com.tailormade.tailor.entities.blocks;

import com.tailormade.tailor.client.menu.DesignerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class DesignerBlock extends FacingBlock {
    public DesignerBlock(BlockBehaviour.Properties properties) {
        super(
                properties.mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.0f)
                        .noOcclusion()
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MenuProvider containerProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("gui.tailormade.designer.title");
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                    return new DesignerMenu(windowId, playerInventory);
                }
            };
            serverPlayer.openMenu(containerProvider, buffer -> {
                buffer.writeBlockPos(pos);
            });
        }
        return InteractionResult.SUCCESS;
    }
}
