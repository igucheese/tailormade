package com.tailormade.tailor.registries;

import com.tailormade.tailor.network.ConfirmBleachPayloadHandler;
import com.tailormade.tailor.network.ConfirmTailorPayloadHandler;
import com.tailormade.tailor.network.SaveDesignPayloadHandler;
import com.tailormade.tailor.network.SaveSkinLayerPayloadHandler;
import com.tailormade.tailor.network.payloads.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.tailormade.tailor.Tailormade.MODID;

@EventBusSubscriber(modid = MODID)
public class ModNetworking {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0.0");
        /**
         * Client to Server
         */
        registrar.playToServer(
                SaveDesignPayload.TYPE,
                SaveDesignPayload.STREAM_CODEC,
                SaveDesignPayloadHandler::handle
        );
        registrar.playToServer(
                ConfirmTailorPayload.TYPE,
                ConfirmTailorPayload.STREAM_CODEC,
                ConfirmTailorPayloadHandler::handle
        );
        registrar.playToServer(
                SaveSkinLayerPayload.TYPE,
                SaveSkinLayerPayload.STREAM_CODEC,
                SaveSkinLayerPayloadHandler::handle
        );
        registrar.playToServer(
                SaveUnderwearPayload.TYPE,
                SaveUnderwearPayload.STREAM_CODEC,
                SaveUnderwearPayload::handle
        );
        registrar.playToServer(
                ConfirmBleachPayload.TYPE,
                ConfirmBleachPayload.STREAM_CODEC,
                ConfirmBleachPayloadHandler::handle
        );
        registrar.playToServer(
                CopyDesignPayload.TYPE,
                CopyDesignPayload.STREAM_CODEC,
                CopyDesignPayload::handle
        );
        registrar.playToServer(
                SavePatternLockPayload.TYPE,
                SavePatternLockPayload.STREAM_CODEC,
                SavePatternLockPayload::handle
        );
        registrar.playToServer(
                RenamePatternPayload.TYPE,
                RenamePatternPayload.STREAM_CODEC,
                RenamePatternPayload::handle
        );
        registrar.playToServer(
                ExportDesignDataStartPayload.TYPE,
                ExportDesignDataStartPayload.STREAM_CODEC,
                ExportDesignDataStartPayload::handle
        );
        registrar.playToServer(
                ExtractDesignPayload.TYPE,
                ExtractDesignPayload.STREAM_CODEC,
                ExtractDesignPayload::handle
        );
        registrar.playToServer(
                ChangeManagerMenuTabPayload.TYPE,
                ChangeManagerMenuTabPayload.STREAM_CODEC,
                ChangeManagerMenuTabPayload::handle
        );

        /**
         * Server to Client
         */
        registrar.playToClient(
                SyncSkinLayerPayload.TYPE,
                SyncSkinLayerPayload.STREAM_CODEC,
                SyncSkinLayerPayload::handle
        );
        registrar.playToClient(
                SyncUnderwearPayload.TYPE,
                SyncUnderwearPayload.STREAM_CODEC,
                SyncUnderwearPayload::handle
        );
        registrar.playToClient(
                SyncDesignPayload.TYPE,
                SyncDesignPayload.STREAM_CODEC,
                SyncDesignPayload::handle
        );
        registrar.playToClient(
                SyncMannequinPayload.TYPE,
                SyncMannequinPayload.STREAM_CODEC,
                SyncMannequinPayload::handle
        );
        registrar.playToClient(
                ExportDesignDataToClientPayload.TYPE,
                ExportDesignDataToClientPayload.STREAM_CODEC,
                ExportDesignDataToClientPayload::handleClient
        );
    }
}
