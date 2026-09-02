package com.tailormade.tailor.registries;

import com.tailormade.tailor.entities.blocks.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.tailormade.tailor.Tailormade.MODID;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    // 仕立て関係ブロック
    public static final DeferredBlock<DesignerBlock> DESIGNER = BLOCKS.register("designer",
            () -> new DesignerBlock(BlockBehaviour.Properties.of())
    );
    public static final DeferredBlock<TailorBlock> TAILOR = BLOCKS.register("tailor",
            () -> new TailorBlock(BlockBehaviour.Properties.of())
    );
    public static final DeferredBlock<BleachingCounterBlock> BLEACHING_COUNTER = BLOCKS.register("bleaching_counter",
            () -> new BleachingCounterBlock(BlockBehaviour.Properties.of())
    );
    public static final DeferredBlock<ManagerBlock> MANAGER = BLOCKS.register("tailor_manager",
            () -> new ManagerBlock(BlockBehaviour.Properties.of())
    );

    public static final DeferredBlock<PowderRoomBlock> POWDER_ROOM = BLOCKS.register("powder_room",
            () -> new PowderRoomBlock(BlockBehaviour.Properties.of())
    );
    public static final DeferredBlock<WardrobeBlock> WARDROBE = BLOCKS.register("wardrobe",
            () -> new WardrobeBlock(BlockBehaviour.Properties.of())
    );

    public static final DeferredBlock<MannequinBlock> MANNEQUIN = BLOCKS.register("mannequin",
            () -> new MannequinBlock(BlockBehaviour.Properties.of())
    );
}
