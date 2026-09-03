package com.tailormade.tailor;

import com.mojang.logging.LogUtils;
import com.tailormade.tailor.client.ClientRenderEvents;
import com.tailormade.tailor.config.ServerConfig;
import com.tailormade.tailor.registries.ModBlockEntities;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.registries.ModMenuTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import static com.tailormade.tailor.registries.ModBlocks.BLOCKS;
import static com.tailormade.tailor.registries.ModBlocks.POWDER_ROOM;
import static com.tailormade.tailor.registries.ModItems.*;

@Mod(Tailormade.MODID)
public class Tailormade {
    public static final String MODID = "tailormade";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAILORMADE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tailormade"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> DESIGNER_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(PATTERN_HELMET.get());
                output.accept(PATTERN_CHESTPLATE.get());
                output.accept(PATTERN_LEGGINGS.get());
                output.accept(PATTERN_BOOTS.get());

                output.accept(DESIGNER_ITEM.get());
                output.accept(TAILOR_ITEM.get());
                output.accept(MANAGER_ITEM.get());
                output.accept(BLEACHING_COUNTER_ITEM.get());
                output.accept(POWDER_ROOM_ITEM.get());
                output.accept(WARDROBE_ITEM.get());

                output.accept(BLEACH.get());
                output.accept(MANNEQUIN_ITEM.get());
            }).build());

    public Tailormade(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModBlockEntities.ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        ModDataComponents.COMPONENTS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientRenderEvents.register(modEventBus, NeoForge.EVENT_BUS);
        }

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {}

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(DESIGNER_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}
