package com.tailormade.tailor.entities.blocks;

import com.tailormade.tailor.entities.blockentities.MannequinEntity;
import com.tailormade.tailor.registries.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MannequinBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 32, 16);
    private MannequinEntity mannequinEntity;

    public MannequinBlock(Properties properties) {
        super(
                properties.noOcclusion()
                        .noCollission()
                        .isValidSpawn((s, l, p, e) -> false)
                        .isRedstoneConductor((s, l, p) -> false)
                        .isSuffocating((s, l, p) -> false)
                        .isViewBlocking((s, l, p) -> false)
                        .strength(0.0f)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;

            level.getEntitiesOfClass(MannequinEntity.class,
                    new AABB(pos).inflate(0.1)
            ).stream().filter(m -> m.distanceToSqr(cx, cy, cz) < 0.5).findFirst().ifPresent(mannequin ->
                    mannequin.interact(player, hand)
            );
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide()) {
            MannequinEntity mannequin = new MannequinEntity(
                    ModBlockEntities.MANNEQUIN.get(), level);
            mannequin.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            float yRot = switch (facing) {
                case NORTH -> 180F;
                case SOUTH -> 0F;
                case WEST  -> 90F;
                case EAST  -> -90F;
                default    -> 0F;
            };
            mannequin.setYRot(yRot);
            mannequin.setFacingYRot(yRot);
            mannequin.yBodyRot = yRot;
            mannequin.yHeadRot = yRot;

            level.addFreshEntity(mannequin);
            this.mannequinEntity = mannequin;
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;
            // 周辺のマネキンエンティティを探して装備をドロップ
            level.getEntitiesOfClass(MannequinEntity.class, new AABB(pos).inflate(0.1))
                .stream().filter(m -> m.distanceToSqr(cx, cy, cz) < 0.5)
                .forEach(mannequin -> {
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
