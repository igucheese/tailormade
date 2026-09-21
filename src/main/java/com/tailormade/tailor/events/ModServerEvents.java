package com.tailormade.tailor.events;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.network.payloads.PrepareCatalogScreenPayload;
import com.tailormade.tailor.network.payloads.SyncDesignPayload;
import com.tailormade.tailor.registries.ModDataComponents;
import com.tailormade.tailor.registries.ModItems;
import com.tailormade.tailor.utils.CatalogService;
import com.tailormade.tailor.utils.InitialTemplateLoader;
import com.tailormade.tailor.utils.files.TemplateLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
            syncCatalogs(player);
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

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        // 書見台にカタログが載っている場合は
        // カタログの GUI を開く
        if (level.getBlockEntity(pos) instanceof LecternBlockEntity lectern) {
            ItemStack book = lectern.getBook();

            if (book.is(ModItems.CATALOG_BOOK.get())) {
                String catalogIdStr = book.get(ModDataComponents.CATALOG_ID.get());
                if (catalogIdStr == null || catalogIdStr.isBlank()) return;
                UUID catalogId = UUID.fromString(catalogIdStr);

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
                if (!level.isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
                    CatalogService.openClientScreen(serverPlayer, catalogId, true, pos);
                }
            }
        }
    }
}
