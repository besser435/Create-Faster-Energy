package me.besser.createfasterenergy.block.charger;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
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


    // WE do the depot check manually. we should probably use the Create preferred way, like this example from Create Crafts and Additions.
//    @Override
//    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
//        super.addBehaviours(behaviours);
//        processingBehaviour =
//                new BeltProcessingBehaviour(this).whenItemEnters((s, i) -> TeslaCoilBeltCallbacks.onItemReceived(s, i, this))
//                        .whileItemHeld((s, i) -> TeslaCoilBeltCallbacks.whenItemHeld(s, i, this));
//        behaviours.add(processingBehaviour);
//    }

    @Override
    public void tick() {
        super.tick();

        // TODO: see how many times things are called each tick. with debug statements.

        if (level == null || level.isClientSide) return;

        float speed = Math.abs(getSpeed());
        boolean canOperate = !isOverStressed() && speed >= minRpm;

        if (canOperate) {
            currentEfficiency = calculateEfficiency(speed);
            feGeneratedThisTick = Math.round((speed * fePerRpm) * currentEfficiency);

            if (feGeneratedThisTick > 0) {
                chargeNearbyDepots();
            }
        } else {
            // Reset state when not operating
            currentEfficiency = 0f;
            feGeneratedThisTick = 0;
        }

        if (level.getGameTime() % 20 == 0) {
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

    private void chargeNearbyDepots() {
        DepotBlockEntity depot = getTargetDepot();
        if (depot == null) return;

        ItemStack stack = depot.getHeldItem();
        if (stack.isEmpty()) return;

        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy == null || !energy.canReceive()) return;


        int accepted = energy.receiveEnergy(feGeneratedThisTick, false);

        if (accepted > 0) {
            depot.setChanged();

            // TODO BUG: this will prevent clients from seeing 100% charge when an item is done.
            if (level.getGameTime() % 10 == 0) { // Only update clients every so often
                depot.notifyUpdate();
            }
        }
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

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        boolean active = !isOverStressed() && Math.abs(getSpeed()) >= minRpm;

        Component status = active
                ? Component.literal("Charging").withStyle(ChatFormatting.GREEN)
                : Component.literal("Idle").withStyle(ChatFormatting.RED);

        // General info
        tooltip.add(Component.literal("    Status: ").withStyle(ChatFormatting.GRAY).append(status));

        if (!active) return true;

        tooltip.add(Component.literal("    Generating: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(feGeneratedThisTick + " FE/t").withStyle(ChatFormatting.AQUA)));

        int effPercent = Math.round(currentEfficiency * 100);
        tooltip.add(Component.literal("    Efficiency: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(effPercent + "%").withStyle(getPercentColor(effPercent))));


        // Item info
        DepotBlockEntity depot = getTargetDepot();

        if (depot != null) {
            ItemStack stack = depot.getHeldItem();
            if (!stack.isEmpty()) {
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
            }
        }

        return true;
    }

    private DepotBlockEntity getTargetDepot() {
        if (level == null) return null;

        BlockEntity be = level.getBlockEntity(worldPosition.below(2));

        return (be instanceof DepotBlockEntity depot) ? depot : null;
    }

    // Make a helper?
    private ChatFormatting getPercentColor(int percent) {
        if (percent >= 85) return ChatFormatting.GREEN;
        if (percent >= 70) return ChatFormatting.YELLOW;
        return ChatFormatting.RED;
    }

    // TODO: make own config values. maybe share most of them like the min and max RPM, but have FE/t for the charger.
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
