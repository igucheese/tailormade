package com.tailormade.tailor.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ChatService {
    public static void sendChat(ServerLevel level, Component component, boolean isActionBar) {
        List<ServerPlayer> players = GeneralService.getPlayers(level);
        for (ServerPlayer player: players) {
            player.displayClientMessage(component, isActionBar);
        }
    }

    public static void sendChat(ServerLevel level, String message, boolean isActionBar) {
        List<ServerPlayer> players = GeneralService.getPlayers(level);
        for (ServerPlayer player: players) {
            player.displayClientMessage(Component.literal(message), isActionBar);
        }
    }

    public static void showMessage(ServerPlayer player, Component component, boolean isActionBar) {
        player.displayClientMessage(component, true);
    }

    public static void showMessage(ServerPlayer player, String message, boolean isActionBar) {
        player.displayClientMessage(Component.literal(message), true);
    }
}