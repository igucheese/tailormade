package com.tailormade.tailor.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.*;

import static com.tailormade.tailor.Tailormade.MODID;

@EventBusSubscriber(modid = MODID)
public class TailormadeCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("tailormade")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("getAllPatterns")
                        .executes(context -> getAllPatterns(context.getSource()))
                )
        );
    }

    private static int getAllPatterns(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Collection<DesignDataRecord> designs = DesignData.get(level).index();
        for (DesignDataRecord d: designs) {
            source.sendSuccess(() -> Component.translatable("command.tailormade.updateAllData.listed", d.name(), d.uuid(), d.designerId()), true);
        }
        return 1;
    }
}
