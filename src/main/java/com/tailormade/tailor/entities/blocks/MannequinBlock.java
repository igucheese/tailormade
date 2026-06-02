package com.tailormade.tailor.entities.blocks;

import com.tailormade.tailor.entities.blockentities.MannequinEntity;
import com.tailormade.tailor.registries.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MannequinBlock extends Block {

    public MannequinBlock(Properties properties) {
        super(
                properties.noOcclusion()
        );
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            net.minecraft.world.entity.LivingEntity placer,
                            ItemStack stack) {
        if (!level.isClientSide()) {
            MannequinEntity mannequin = new MannequinEntity(
                    ModBlockEntities.MANNEQUIN.get(), level);
            // ブロックの中央・少し上に配置
            mannequin.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            level.addFreshEntity(mannequin);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            // 周辺のマネキンエンティティを探して装備をドロップ
            level.getEntitiesOfClass(MannequinEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(0.5)
            ).forEach(mannequin -> {
                for (EquipmentSlot slot : new EquipmentSlot[]{
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                        EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                    ItemStack armor = mannequin.getItemBySlot(slot);
                    if (!armor.isEmpty()) {
                        Block.popResource(level, pos, armor);
                    }
                }
                mannequin.discard();
            });
        }
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }
}
