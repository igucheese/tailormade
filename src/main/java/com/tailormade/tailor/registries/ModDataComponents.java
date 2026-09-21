package com.tailormade.tailor.registries;

import com.mojang.serialization.Codec;
import com.tailormade.tailor.data.PixelData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.tailormade.tailor.Tailormade.MODID;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PATTERN_ID =
            COMPONENTS.register("pattern_id",
                    () -> DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PixelData>> PIXEL_DATA =
            COMPONENTS.register("pixel_data",
                    () -> DataComponentType.<PixelData>builder()
                            .persistent(PixelData.CODEC)
                            .networkSynchronized(PixelData.STREAM_CODEC)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PATTERN_NAME =
            COMPONENTS.register("pattern_name",
                    () -> DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CLOTH_NAME =
            COMPONENTS.register("cloth_name",
                    () -> DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> TAILOR_NAME =
            COMPONENTS.register("tailor_name",
                    () -> DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SERIAL_NUMBER =
            COMPONENTS.register("serial_number",
                    () -> DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_COPIED =
            COMPONENTS.register("is_copied",
                    () -> DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_EXTRACTED =
            COMPONENTS.register("is_extracted",
                    () -> DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IS_IMPORTED =
            COMPONENTS.register("is_imported",
                    () -> DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CATALOG_ID =
            COMPONENTS.register("catalog_id",
                    () -> DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build());
}
