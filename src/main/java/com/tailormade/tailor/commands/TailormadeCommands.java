package com.tailormade.tailor.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.utils.OldDataMerger;
import com.tailormade.tailor.utils.files.TemplateConverter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
                .then(Commands.literal("create")
                        .then(Commands.literal("template")
                                .then(Commands.argument("inputFileName", StringArgumentType.string())
                                        .then(Commands.argument("outputFileName", StringArgumentType.string())
                                                .executes(context -> {
                                                    String inputFileName = StringArgumentType.getString(context, "inputFileName");
                                                    String outputFileName = StringArgumentType.getString(context, "outputFileName");
                                                    return createPatternTemplate(context.getSource(), inputFileName, outputFileName);
                                                })
                                        )
                                )
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

    private static int createPatternTemplate(CommandSourceStack source, String inputFileName, String outputFileName) {
        MinecraftServer server = source.getServer();

        Path inputDir = server.getServerDirectory().toAbsolutePath().resolve("tailormade_exports");
        Path inputFile = inputDir.resolve(inputFileName + ".nbt");

        Path outputDir = server.getServerDirectory().toAbsolutePath().resolve("tailormade_templates");
        Path outputFile = outputDir.resolve(outputFileName + ".json");

        if (!Files.exists(inputFile)) {
            source.sendFailure(Component.literal("[TailorMade] Input file not found: " + inputFile));
            return 0;
        }

        try {
            CompoundTag exportedNbt = NbtIo.read(inputFile);
            if (exportedNbt == null) {
                source.sendFailure(Component.literal("[TailorMade] Failed to read NBT (empty or corrupt): " + inputFile));
                return 0;
            }

            String json = TemplateConverter.toTemplateJson(exportedNbt);

            Files.createDirectories(outputDir);
            Files.writeString(outputFile, json, StandardCharsets.UTF_8);

            source.sendSuccess(() -> Component.literal("[TailorMade] Converted template: " + outputFile), true);
            return 1;
        } catch (IOException e) {
            source.sendFailure(Component.literal("[TailorMade] Conversion failed: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
}
