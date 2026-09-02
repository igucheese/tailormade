package com.tailormade.tailor.utils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;

public class SoundService {
    public static void playSound(Player player, SoundEvent event, float volume) {
        player.playNotifySound(event, SoundSource.NEUTRAL, volume, 1.0F);
    }

    public static void playToAll(ServerLevel level, SoundEvent event, float volume) {
        for (ServerPlayer player : GeneralService.getPlayers(level)) {
            playSound(player, event, volume);
        }
    }

    public static void playWithRegistry(ServerLevel level, ResourceKey<?> resource, float volume) {
        Registry<Instrument> registry = level.getServer().registryAccess().registryOrThrow(Registries.INSTRUMENT);
        Instrument instrument = registry.get((ResourceKey<Instrument>) resource);

        if (instrument != null) {
            SoundEvent soundEvent = instrument.soundEvent().value();
            playToAll(level, soundEvent, volume);
        }
    }
}
