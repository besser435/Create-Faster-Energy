package me.besser.createfasterenergy.blocks;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import me.besser.createfasterenergy.CreateFasterEnergy;
import me.besser.createfasterenergy.blocks.alternator.AlternatorBlockEntity;
import me.besser.createfasterenergy.blocks.alternator.AlternatorRenderer;
import me.besser.createfasterenergy.blocks.time_motor.TimeMotorBlockEntity;

public class FEBlockEntities {
    public static final BlockEntityEntry<AlternatorBlockEntity> ALTERNATOR = CreateFasterEnergy.REGISTRATE
            .blockEntity("alternator", AlternatorBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false)
            .validBlocks(FEBlocks.ALTERNATOR)
            .renderer(() -> AlternatorRenderer::new)
            .register();

    public static final BlockEntityEntry<TimeMotorBlockEntity> TIME_MOTOR = CreateFasterEnergy.REGISTRATE
            .blockEntity("time_motor", TimeMotorBlockEntity::new)
            //.visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false)
            .validBlocks(FEBlocks.TIME_MOTOR)
            //.renderer(() -> TimeMotorRenderer::new)
            .register();

    public static void register() {}    // Needed for some reason
}