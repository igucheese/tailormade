package com.tailormade.tailor.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tailormade.tailor.entities.blockentities.MannequinEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

import static com.tailormade.tailor.Tailormade.MODID;

public class MannequinModel extends EntityModel<MannequinEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MODID, "mannequin"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public MannequinModel(ModelPart root) {
        this.head     = root.getChild("head");
        this.body     = root.getChild("body");
        this.leftArm  = root.getChild("leftArm");
        this.rightArm = root.getChild("rightArm");
        this.rightLeg = root.getChild("rightLeg");
        this.leftLeg  = root.getChild("leftLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4F, -8F, -4F, 8, 8, 8, new CubeDeformation(0F)),
                PartPose.offset(0F, 0F, 0F));

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4F, 0F, -2F, 8, 12, 4, new CubeDeformation(0F)),
                PartPose.offset(0F, 0F, 0F));

        root.addOrReplaceChild("leftArm",
                CubeListBuilder.create()
                        .texOffs(32, 48).addBox(-3F, -2F, -2F, 4, 12, 4, new CubeDeformation(0F)),
                PartPose.offset(-5F, 2F, 0F));

        root.addOrReplaceChild("rightArm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-1F, -2F, -2F, 4, 12, 4, new CubeDeformation(0F)),
                PartPose.offset(5F, 2F, 0F));

        root.addOrReplaceChild("rightLeg",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2F, 0F, -2F, 4, 12, 4, new CubeDeformation(0F)),
                PartPose.offset(2F, 12F, 0F));

        root.addOrReplaceChild("leftLeg",
                CubeListBuilder.create()
                        .texOffs(16, 48).addBox(-2F, 0F, -2F, 4, 12, 4, new CubeDeformation(0F)),
                PartPose.offset(-2F, 12F, 0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(MannequinEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // マネキンは静止ポーズ固定
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               int color) {
        head.render(poseStack, buffer, packedLight, packedOverlay, color);
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
        leftArm.render(poseStack, buffer, packedLight, packedOverlay, color);
        rightArm.render(poseStack, buffer, packedLight, packedOverlay, color);
        rightLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
        leftLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
