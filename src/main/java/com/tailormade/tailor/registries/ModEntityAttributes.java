package com.tailormade.tailor.registries;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import static com.tailormade.tailor.Tailormade.MODID;

@EventBusSubscriber(modid = MODID)
public class ModEntityAttributes {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModBlockEntities.MANNEQUIN.get(),
                LivingEntity.createLivingAttributes().build()
        );
    }
}