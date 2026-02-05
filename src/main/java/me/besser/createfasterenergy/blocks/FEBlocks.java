package me.besser.createfasterenergy.blocks;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import me.besser.createfasterenergy.CreateFasterEnergy;
import me.besser.createfasterenergy.blocks.alternator.AlternatorBlock;
import me.besser.createfasterenergy.blocks.time_motor.TimeMotorBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class FEBlocks {
    // TODO: Creative tab

    public static final BlockEntry<AlternatorBlock> ALTERNATOR = CreateFasterEnergy.REGISTRATE.block("alternator", AlternatorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p
                    .noOcclusion()
                    .sound(SoundType.NETHERITE_BLOCK)
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()
            )
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .tag(BlockTags.NEEDS_IRON_TOOL)
            .lang("Alternator")
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<TimeMotorBlock> TIME_MOTOR = CreateFasterEnergy.REGISTRATE.block("time_motor", TimeMotorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .tag(BlockTags.NEEDS_IRON_TOOL)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .item()
            .transform(customItemModel())
            .register();

    public static void register() {}    // Needed for some reason
}
