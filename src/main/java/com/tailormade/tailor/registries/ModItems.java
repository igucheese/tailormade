package com.tailormade.tailor.registries;

import com.tailormade.tailor.data.PatternType;
import com.tailormade.tailor.entities.items.PatternItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.tailormade.tailor.Tailormade.MODID;
import static com.tailormade.tailor.registries.ModBlocks.*;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // 型紙
    public static final DeferredItem<Item> PATTERN = ITEMS.registerSimpleItem("pattern", new Item.Properties());
    public static final DeferredItem<PatternItem> PATTERN_HELMET = ITEMS.register("pattern_helmet", () -> new PatternItem(PatternType.HEAD, new Item.Properties()));
    public static final DeferredItem<PatternItem> PATTERN_CHESTPLATE = ITEMS.register("pattern_chestplate", () -> new PatternItem(PatternType.CHEST, new Item.Properties()));
    public static final DeferredItem<PatternItem> PATTERN_LEGGINGS = ITEMS.register("pattern_leggings", () -> new PatternItem(PatternType.LEGS, new Item.Properties()));
    public static final DeferredItem<PatternItem> PATTERN_BOOTS = ITEMS.register("pattern_boots", () -> new PatternItem(PatternType.FEET, new Item.Properties()));

    // 仕立て関係ブロック
    public static final DeferredItem<BlockItem> DESIGNER_ITEM = ITEMS.registerSimpleBlockItem("designer", DESIGNER);
    public static final DeferredItem<BlockItem> TAILOR_ITEM = ITEMS.registerSimpleBlockItem("tailor", TAILOR);
    public static final DeferredItem<BlockItem> BLEACHING_COUNTER_ITEM = ITEMS.registerSimpleBlockItem("bleaching_counter", BLEACHING_COUNTER);
    public static final DeferredItem<BlockItem> POWDER_ROOM_ITEM = ITEMS.registerSimpleBlockItem("powder_room", POWDER_ROOM);
    public static final DeferredItem<BlockItem> WARDROBE_ITEM = ITEMS.registerSimpleBlockItem("wardrobe", WARDROBE);

    // その他雑貨
    public static final DeferredItem<Item> BLEACH = ITEMS.registerSimpleItem("bleach", new Item.Properties());
    public static final DeferredItem<BlockItem> MANNEQUIN_ITEM = ITEMS.registerSimpleBlockItem("mannequin", MANNEQUIN);
}
