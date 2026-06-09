package com.tailormade.tailor.entities.blockentities;

import com.tailormade.tailor.data.MannequinPose;
import com.tailormade.tailor.network.payloads.SyncMannequinPayload;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

public class MannequinEntity extends LivingEntity {

    public final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    public final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private float facingYRot = 0F;
    private static final EntityDataAccessor<Integer> POSE_ID = SynchedEntityData.defineId(MannequinEntity.class, EntityDataSerializers.INT);

    public MannequinEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return facingYRot;
    }

    public void setFacingYRot(float yRot) {
        this.facingYRot = yRot;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {}

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return armorItems;
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return handItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot) {
            case FEET  -> armorItems.get(0);
            case LEGS  -> armorItems.get(1);
            case CHEST -> armorItems.get(2);
            case HEAD  -> armorItems.get(3);
            case MAINHAND -> handItems.get(0);
            case OFFHAND  -> handItems.get(1);
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        switch (slot) {
            case FEET  -> armorItems.set(0, stack);
            case LEGS  -> armorItems.set(1, stack);
            case CHEST -> armorItems.set(2, stack);
            case HEAD  -> armorItems.set(3, stack);
            case MAINHAND -> handItems.set(0, stack);
            case OFFHAND  -> handItems.set(1, stack);
        }

        if (!level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntity(this,
                    new SyncMannequinPayload(this.getId(), slot.ordinal(), stack.copy())
            );
        }
    }

    @Override
    public boolean isPickable() { return true; }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    protected float getStandingEyeHeight(
            Pose pose,
            EntityDimensions dims) {
        return 1.62F;
    }

    public void setArmorItem(EquipmentSlot slot, ItemStack stack) {
        setItemSlot(slot, stack.copy());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty() && held.getItem() instanceof ArmorItem armor) {
            EquipmentSlot slot = armor.getEquipmentSlot();
            ItemStack current = getItemBySlot(slot);

            if (!current.isEmpty() && !player.getInventory().add(current)) {
                player.drop(current, false);
            }
            setItemSlot(slot, held.copy());
            if (!player.isCreative()) held.shrink(1);

            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (held.isEmpty()) {
            for (EquipmentSlot slot : new EquipmentSlot[]{
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack current = getItemBySlot(slot);
                if (!current.isEmpty()) {
                    if (!player.getInventory().add(current)) player.drop(current, false);
                    setItemSlot(slot, ItemStack.EMPTY);
                    return InteractionResult.sidedSuccess(level().isClientSide());
                }
            }
            if (!level().isClientSide()) {
                cyclePose();
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        return InteractionResult.PASS;
    }

    public MannequinPose getMannequinPose() {
        int id = entityData.get(POSE_ID);
        MannequinPose[] values = MannequinPose.values();
        return values[id];
    }

    public void cyclePose() {
        MannequinPose next = getMannequinPose().next();
        entityData.set(POSE_ID, next.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        ListTag list = tag.getList("MannequinArmorItems", 10);
        for (int i = 0; i < list.size() && i < armorItems.size(); i++) {
            armorItems.set(i, ItemStack.parseOptional(registryAccess(), list.getCompound(i)));
        }
        entityData.set(POSE_ID, tag.getInt("PoseId"));
        facingYRot = tag.getFloat("FacingYRot");
        this.setYRot(facingYRot);
        this.yBodyRot = facingYRot;
        this.yHeadRot = facingYRot;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        ListTag list = new ListTag();
        for (ItemStack stack : armorItems) {
            list.add(stack.saveOptional(registryAccess()));
        }
        tag.put("MannequinArmorItems", list);
        tag.putInt("PoseId", entityData.get(POSE_ID));
        tag.putFloat("FacingYRot", facingYRot);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(POSE_ID, 0);
    }
}
