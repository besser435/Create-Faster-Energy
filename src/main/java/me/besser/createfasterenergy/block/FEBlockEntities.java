package me.besser.createfasterenergy.block;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import me.besser.createfasterenergy.CreateFasterEnergy;
import me.besser.createfasterenergy.block.alternator.AlternatorBlockEntity;
import me.besser.createfasterenergy.block.alternator.AlternatorRenderer;
import me.besser.createfasterenergy.block.charger.ChargerBlockEntity;
import me.besser.createfasterenergy.block.charger.ChargerRenderer;
import me.besser.createfasterenergy.block.time_motor.TimeMotorBlockEntity;

public class FEBlockEntities {
    public static final BlockEntityEntry<AlternatorBlockEntity> ALTERNATOR = CreateFasterEnergy.REGISTRATE
            .blockEntity("alternator", AlternatorBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false)
            .validBlocks(FEBlocks.ALTERNATOR)
            .renderer(() -> AlternatorRenderer::new)
            .register();

    public static final BlockEntityEntry<ChargerBlockEntity> CHARGER = CreateFasterEnergy.REGISTRATE
            .blockEntity("charger", ChargerBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.backHorizontal(AllPartialModels.SHAFT), false)
            .validBlocks(FEBlocks.CHARGER)
            //.renderer(() -> ChargerRenderer::new)
            .register();

    public static final BlockEntityEntry<TimeMotorBlockEntity> TIME_MOTOR = CreateFasterEnergy.REGISTRATE
            .blockEntity("time_motor", TimeMotorBlockEntity::new)
            //.visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false)
            .validBlocks(FEBlocks.TIME_MOTOR)
            //.renderer(() -> TimeMotorRenderer::new)
            .register();

    public static void register() {}    // Needed for some reason
}