package me.besser.createfasterenergy.block;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractEnergyMakingKineticBE extends KineticBlockEntity {

    // Config
    protected float baseStressImpact;
    protected float optimalRpm;
    protected float maxRpm;
    protected float maxEfficiency;
    protected float minEfficiency;

    // State
    protected float currentEfficiency = 1.0f;
    protected int feGeneratedThisTick = 0;

    public AbstractEnergyMakingKineticBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected float calculateEfficiency(float speed) {
        if (speed <= optimalRpm) return maxEfficiency;

        float excessSpeed = Math.min(speed, maxRpm) - optimalRpm;
        float excessRange = maxRpm - optimalRpm;
        float falloff = excessSpeed / excessRange;

        return maxEfficiency - (falloff * (maxEfficiency - minEfficiency));
    }

    @Override
    public float calculateStressApplied() {
        return baseStressImpact;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket) {
        tag.putFloat("Efficiency", currentEfficiency);
        tag.putInt("GenRate", feGeneratedThisTick);
        super.write(tag, provider, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket) {
        super.read(tag, provider, clientPacket);
        currentEfficiency = tag.getFloat("Efficiency");
        feGeneratedThisTick = tag.getInt("GenRate");
    }
}