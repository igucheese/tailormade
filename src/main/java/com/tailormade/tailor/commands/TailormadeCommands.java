package com.tailormade.tailor.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.utils.OldDataMerger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
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
                .then(Commands.literal("oldData")
                        .then(Commands.literal("count")
                                .executes(context -> countOldData(context.getSource()))
                        )
                        .then(Commands.literal("merge")
                                .executes(context -> mergeOldData(context.getSource()))
                        )
                        .then(Commands.literal("cleanUp")
                                .executes(context -> cleanUpOldData(context.getSource()))
                        )
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

    private static int countOldData(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        int counted = OldDataMerger.countMergeRequiredDesigns(server);
        source.sendSuccess(() -> Component.translatable("command.tailormade.updateAllData.count_merge_required", counted), true);
        return counted;
    }
    private static int mergeOldData(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        int processed = OldDataMerger.mergeAllDimensionsData(server);
        source.sendSuccess(() -> Component.translatable("command.tailormade.updateAllData.merged", processed), true);
        return processed;
    }
    private static int cleanUpOldData(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        int counted = OldDataMerger.cleanUp(server);
        source.sendSuccess(() -> Component.translatable("command.tailormade.updateAllData.clean_up", counted), true);
        return counted;
    }
}
