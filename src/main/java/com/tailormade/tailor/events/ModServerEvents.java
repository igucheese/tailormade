package com.tailormade.tailor.events;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.network.payloads.SyncDesignPayload;
import com.tailormade.tailor.utils.InitialTemplateLoader;
import com.tailormade.tailor.utils.files.TemplateLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.tailormade.tailor.Tailormade.MODID;
import static com.tailormade.tailor.events.SyncDataLayers.*;

@EventBusSubscriber(modid = MODID)
public class ModServerEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Tailormade.LOGGER.info("[SYNC] Starting data sync...");
            ServerLevel level = player.serverLevel();

            addMeIfAbsent(player);
            syncSkinLayer(player);
            syncUnderwearLayer(player);
            syncDesignData(player);
            syncGlobalPlayers(player);
            syncDesignTemplates(player);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
//        Command.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        Path templatesDir = event.getServer().getServerDirectory().toAbsolutePath()
                .resolve("config").resolve("tailormade").resolve("templates");
        // まずデフォルトテンプレート集をコピペ
        InitialTemplateLoader.installDefaults(templatesDir);
        // してから、config を見に行く
        int loaded = TemplateLoader.loadAll(templatesDir);
        Tailormade.LOGGER.info("Loaded {} design templates!", loaded);
    }
}
