package com.tailormade.tailor.client;

import com.tailormade.tailor.client.model.MannequinModel;
import com.tailormade.tailor.client.renderer.*;
import com.tailormade.tailor.registries.ModBlockEntities;
import com.tailormade.tailor.registries.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

import java.util.List;

public class ClientRenderEvents {
    public static void register(IEventBus modEventBus, IEventBus forgeBus) {
        modEventBus.addListener(ClientRenderEvents::onAddLayers);
        forgeBus.addListener(ClientRenderEvents::onEquipmentChange);
        modEventBus.addListener(ClientRenderEvents::onRegisterLayerDefinitions);
        modEventBus.addListener(ClientRenderEvents::onRegisterRenderers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            EntityRenderer<?> renderer = event.getSkin(skin);
            if (!(renderer instanceof PlayerRenderer playerRenderer)) continue;

            List<RenderLayer<AbstractClientPlayer, ?>> layers =
                    (List<RenderLayer<AbstractClientPlayer, ?>>) (List<?>) playerRenderer.layers;

            for (int i = 0; i < layers.size(); i++) {
                if (!(layers.get(i) instanceof HumanoidArmorLayer<?, ?, ?> original)) continue;

                HumanoidModel innerModel = original.innerModel;
                HumanoidModel outerModel = original.outerModel;
                ModelManager  modelManager = Minecraft.getInstance().getModelManager();

                layers.set(i, new TailorHumanoidArmorLayer(
                        playerRenderer, innerModel, outerModel, modelManager));
                break;
            }

            playerRenderer.addLayer(new SkinLayerRenderLayer(playerRenderer));
            playerRenderer.addLayer(new TailorArmorRenderLayer(playerRenderer));
        }
    }

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModBlockEntities.MANNEQUIN.get(),
                MannequinRenderer::new
        );
    }

    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                MannequinModel.LAYER_LOCATION,
                MannequinModel::createBodyLayer
        );
    }

    private static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;

        ItemStack oldStack = event.getFrom();
        ItemStack newStack = event.getTo();

        if (oldStack.has(ModDataComponents.PATTERN_ID.get()) ||
                newStack.has(ModDataComponents.PATTERN_ID.get())) {
            TailorTextureCompositor.invalidate(player.getUUID());
        }
    }
}