package com.tailormade.tailor.data;

import com.tailormade.tailor.data.records.LayerData;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record DesignDataRecord(
        UUID uuid,
        PixelData pixelData,
        List<LayerData> layers,
        UUID userId,
        String name,
        String type,
        boolean isLocked,
        UUID designerId, // オリジナルのデザイナーUUID。不変。
        boolean isSlim // 体型フラグ
) {
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("uuid", uuid);
        nbt.put("pixelData", new IntArrayTag(pixelData.getPixels()));

        ListTag layersTag = new ListTag();
        for (LayerData layer : this.layers) {
            layersTag.add(layer.save());
        }
        nbt.put("layers", layersTag);

        nbt.putUUID("userId", userId);
        nbt.putString("name", name);
        nbt.putString("type", type);
        nbt.putBoolean("isLocked", isLocked);
        nbt.putUUID("designerId", designerId);
        nbt.putBoolean("isSlim", isSlim);
        return nbt;
    }

    public static DesignDataRecord load(CompoundTag nbt) {
        UUID uuid = nbt.getUUID("uuid");
        int[] pixels = nbt.getIntArray("pixelData");
        PixelData pixelData = new PixelData(pixels);

        ListTag layersTag = nbt.getList("layers", Tag.TAG_COMPOUND);
        List<LayerData> layers = new ArrayList<>();
        for (int i = 0; i < layersTag.size(); i++) {
            CompoundTag layerNbt = layersTag.getCompound(i);
            layers.add(LayerData.load(layerNbt));
        }

        UUID userId = nbt.getUUID("userId");
        String name = nbt.getString("name");
        String type = nbt.getString("type");
        boolean isLocked = nbt.contains("isLocked") ? nbt.getBoolean("isLocked") : false;
        UUID designerId = nbt.contains("designerId") ? nbt.getUUID("designerId") : userId;
        boolean isSlim = nbt.contains("isSlim") ? nbt.getBoolean("isSlim") : false;

        return new DesignDataRecord(uuid, pixelData, layers, userId, name, type, isLocked, designerId, isSlim);
    }

    public DesignDataRecord withLocked() {
        return new DesignDataRecord(uuid, pixelData, layers, userId, name, type, true, designerId, isSlim);
    }
    public DesignDataRecord withUnlocked() {
        return new DesignDataRecord(uuid, pixelData, layers, userId, name, type, false, designerId, isSlim);
    }
    public DesignDataRecord withName(String newName) {
        return new DesignDataRecord(uuid, pixelData, layers, userId, newName, type, isLocked, designerId, isSlim);
    }
    public DesignDataRecord withOriginalDesigner(UUID designer) {
        return new DesignDataRecord(uuid, pixelData, layers, userId, name, type, isLocked, designer, isSlim);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, List<LayerData>> LAYERS_STREAM_CODEC =
            ByteBufCodecs.collection(ArrayList::new, LayerData.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, DesignDataRecord> STREAM_CODEC = StreamCodec.of(
            (buf, info) -> {
                buf.writeUUID(info.uuid());
                PixelData.STREAM_CODEC.encode(buf, info.pixelData());
                LAYERS_STREAM_CODEC.encode(buf, info.layers());
                buf.writeUUID(info.userId());
                buf.writeUtf(info.name());
                buf.writeUtf(info.type());
                buf.writeBoolean(info.isLocked());
                buf.writeUUID(info.designerId());
                buf.writeBoolean(info.isSlim());
            },
            buf -> {
                return new DesignDataRecord(
                        buf.readUUID(),
                        PixelData.STREAM_CODEC.decode(buf),
                        LAYERS_STREAM_CODEC.decode(buf),
                        buf.readUUID(),
                        buf.readUtf(),
                        buf.readUtf(),
                        buf.readBoolean(),
                        buf.readUUID(),
                        buf.readBoolean()
                );
            }
    );
}
