package com.tailormade.tailor.utils;

import com.tailormade.tailor.data.GlobalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
import java.util.UUID;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class GeneralService {
    public static List<ServerPlayer> getPlayers(ServerLevel level) {
        PlayerList playerList = level.getServer().getPlayerList();
        return playerList.getPlayers();
    }

    public static GlobalPlayer composeNewPlayer(UUID uuid, String name) {
        return new GlobalPlayer(uuid, name);
    }
}
