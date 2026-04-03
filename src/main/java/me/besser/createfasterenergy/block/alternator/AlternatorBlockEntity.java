package me.besser.createfasterenergy.block.alternator;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import me.besser.createfasterenergy.util.FEConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class AlternatorBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {
    //private static int DATA_VERSION = 1;

    // Config
    private float baseStressImpact;
    private float minRpm;
    private float maxRpm;
    private float fePerRpm;

    private float optimalRpm;
    private float maxEfficiency;
    private float minEfficiency;

    // State
    private float currentEfficiency = 1.0f;
    private int FeGeneratedThisTick = 0;
    private int FePushedThisTick = 0;

    public AlternatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // TODO: can the energy stuff be optimized? I think there were energy caches or something.
    // The capability check happens on all 6 sides and is done every tick.
    // Should maybe wait, as in NeoForge 21.9.1 The way energy is handled got redone.
    public final IEnergyStorage energyHandler = new IEnergyStorage() {
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return Math.min(FeGeneratedThisTick, maxExtract);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }

        @Override
        public int getEnergyStored() { return 0; }

        @Override
        public int getMaxEnergyStored() { return 0; }

        @Override
        public boolean canExtract() { return true; }

        @Override
        public boolean canReceive() { return false; }
    };

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;

        float speed = Math.abs(getSpeed());

        if (!isOverStressed() && speed >= minRpm) {
            currentEfficiency = calculateEfficiency(speed);
            FeGeneratedThisTick = Math.round((speed * fePerRpm) * currentEfficiency);

            if (FeGeneratedThisTick > 0) {
                FePushedThisTick = pushEnergy();
            } else {
                FePushedThisTick = 0;
            }
        } else {
            currentEfficiency = 0;
            FeGeneratedThisTick = 0;
        }

        if ((level.getGameTime() % 20) == 0) {
            notifyUpdate();
            updateConfigValues();
        }
    }

    private float calculateEfficiency(float speed) {
        if (speed <= optimalRpm) return maxEfficiency;

        float excessSpeed = Math.min(speed, maxRpm) - optimalRpm;
        float excessRange = maxRpm - optimalRpm;
        float falloff = excessSpeed / excessRange;

        return maxEfficiency - (falloff * (maxEfficiency - minEfficiency));
    }

    private int pushEnergy() {
        int energyLeftToPush = FeGeneratedThisTick;
        int totalAccepted = 0;

        for (Direction side : Direction.values()) {
            if (energyLeftToPush <= 0) break;

            IEnergyStorage neighbor = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    worldPosition.relative(side), side.getOpposite());

            if (neighbor != null && neighbor.canReceive()) {
                int accepted = neighbor.receiveEnergy(energyLeftToPush, false);
                energyLeftToPush -= accepted;
                totalAccepted += accepted;
            }
        }

        return totalAccepted;
    }

    @Override
    public float calculateStressApplied() {
        return baseStressImpact;
    }

    @Override
    public boolean isSpeedRequirementFulfilled() {
        return Math.abs(getSpeed()) >= minRpm;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        // Status
        boolean active = !isOverStressed() && Math.abs(getSpeed()) >= minRpm;
        Component status = active ?
                Component.literal("Active").withStyle(ChatFormatting.GREEN) :
                Component.literal("Idle").withStyle(ChatFormatting.RED);
        tooltip.add(Component.literal("    Status: ").withStyle(ChatFormatting.GRAY).append(status));

        if (!active) return true;

        // Generation
        tooltip.add(Component.literal("    Generating: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(FeGeneratedThisTick + " FE/t").withStyle(ChatFormatting.AQUA)));

        // Usage
        tooltip.add(Component.literal("    Usage: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(FePushedThisTick + " FE/t").withStyle(ChatFormatting.GOLD)));

        // Efficiency
        int effPercent = Math.round(currentEfficiency * 100);
        tooltip.add(Component.literal("    Efficiency: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(effPercent + "%").withStyle(getPercentColor(effPercent))));

        return true;
    }


    // Make a helper?
    private ChatFormatting getPercentColor(int percent) {
        if (percent >= 85) return ChatFormatting.GREEN;
        if (percent >= 70) return ChatFormatting.YELLOW;
        return ChatFormatting.RED;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateConfigValues();
    }

    private void updateConfigValues() {
        // Pull values from config (Create uses floats, so cast to that instead of doubles)
        // NeoForge uses hot loading, so we need to update them.
        this.baseStressImpact = FEConfig.COMMON.baseStressImpact.get().floatValue();
        this.minRpm = FEConfig.COMMON.minRpm.get().floatValue();
        this.maxRpm = FEConfig.COMMON.maxRpm.get().floatValue();
        this.fePerRpm = FEConfig.COMMON.fePerRpm.get().floatValue();

        this.optimalRpm = FEConfig.COMMON.optimalRpm.get().floatValue();
        this.maxEfficiency = FEConfig.COMMON.maxEfficiency.get().floatValue();
        this.minEfficiency = FEConfig.COMMON.minEfficiency.get().floatValue();
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket) {
        tag.putFloat("Efficiency", currentEfficiency);
        tag.putInt("GenRate", FeGeneratedThisTick);
        tag.putInt("PushRate", FePushedThisTick);
        super.write(tag, provider, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket) {
        super.read(tag, provider, clientPacket);
        currentEfficiency = tag.getFloat("Efficiency");
        FeGeneratedThisTick = tag.getInt("GenRate");
        FePushedThisTick = tag.getInt("PushRate");
    }
}
