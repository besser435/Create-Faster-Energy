package me.besser.createfasterenergy.block.charger;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import me.besser.createfasterenergy.util.FEConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class ChargerBlockEntity extends KineticBlockEntity {

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
    private int feGeneratedThisTick = 0;

    public ChargerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        // Process on belts, depots, and weighted ejectors
        behaviours.add(new BeltProcessingBehaviour(this)
                .whenItemEnters(this::onItemReceived)
                .whileItemHeld(this::whenItemHeld));
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;

        float speed = Math.abs(getSpeed());
        boolean canOperate = !isOverStressed() && speed >= minRpm;

        if (canOperate) {
            currentEfficiency = calculateEfficiency(speed);
            feGeneratedThisTick = Math.round((speed * fePerRpm) * currentEfficiency);
        } else {
            currentEfficiency = 0f;
            feGeneratedThisTick = 0;
        }

        if (level.getGameTime() % 20 == 0) {
            notifyUpdate();
            updateConfigValues();
        }
    }

    // Pass item unless it can be charged
    private ProcessingResult onItemReceived(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        if (feGeneratedThisTick <= 0) return ProcessingResult.PASS;

        IEnergyStorage energy = transported.stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy == null || !energy.canReceive()) return ProcessingResult.PASS;

        if (energy.getEnergyStored() >= energy.getMaxEnergyStored()) {
            return ProcessingResult.PASS;
        }

        return ProcessingResult.HOLD;
    }

    // Hold and charge item
    private ProcessingResult whenItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        if (feGeneratedThisTick <= 0) return ProcessingResult.PASS; // Lost power while holding, let it go

        IEnergyStorage energy = transported.stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy == null || !energy.canReceive()) return ProcessingResult.PASS;

        int accepted = energy.receiveEnergy(feGeneratedThisTick, false);

        if (accepted > 0) {
            if (level.getGameTime() % 10 == 0) {    // Send periodic updates to clients while charging so they see the progress
                handler.blockEntity.notifyUpdate();
            }

            return ProcessingResult.HOLD;
        }

        // Accepted == 0 means the item is completely full, and needs a final client update to show 100%
        handler.blockEntity.notifyUpdate();
        return ProcessingResult.PASS;
    }


    // Stress and energy calculations
    private float calculateEfficiency(float speed) {
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
    public boolean isSpeedRequirementFulfilled() {
        return Math.abs(getSpeed()) >= minRpm;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateConfigValues();
    }


    // Goggle info and client syncing
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        boolean active = !isOverStressed() && Math.abs(getSpeed()) >= minRpm;

        Component status = active
                ? Component.literal("Charging").withStyle(ChatFormatting.GREEN)
                : Component.literal("Idle").withStyle(ChatFormatting.RED);

        tooltip.add(Component.literal("    Status: ").withStyle(ChatFormatting.GRAY).append(status));

        if (!active) return true;

        tooltip.add(Component.literal("    Generating: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(feGeneratedThisTick + " FE/t").withStyle(ChatFormatting.AQUA)));

        int effPercent = Math.round(currentEfficiency * 100);
        tooltip.add(Component.literal("    Efficiency: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(effPercent + "%").withStyle(getPercentColor(effPercent))));


        // We keep this purely for static Depots so players can see the exact item charge in the goggles.
        // TODO: either get rid of this or make it work for all types. its kind of janky only having it for depots.
        DepotBlockEntity depot = getTargetDepotForTooltip();
        if (depot == null) return false;

        ItemStack stack = depot.getHeldItem();
        if (stack.isEmpty()) return false;

        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);

        if (energy != null) {
            int stored = energy.getEnergyStored();
            int max = energy.getMaxEnergyStored();

            if (max > 0) {
                int percent = (int) ((stored / (float) max) * 100);
                tooltip.add(Component.literal("    Item Charge: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(percent + "%")
                                .withStyle(getPercentColor(percent))));
            }
        }

        return true;
    }

    private DepotBlockEntity getTargetDepotForTooltip() {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.below(2));
        return (be instanceof DepotBlockEntity depot) ? depot : null;
    }

    // TODO: Make a helper class?
    private ChatFormatting getPercentColor(int percent) {
        if (percent >= 85) return ChatFormatting.GREEN;
        if (percent >= 70) return ChatFormatting.YELLOW;
        return ChatFormatting.RED;
    }

    // TODO: Redo config and make discrete sections for alternator and charger
    private void updateConfigValues() {
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