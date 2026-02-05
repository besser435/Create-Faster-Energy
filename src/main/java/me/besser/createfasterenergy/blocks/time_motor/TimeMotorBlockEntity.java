package me.besser.createfasterenergy.blocks.time_motor;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import me.besser.createfasterenergy.items.TimeInBottleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TimeMotorBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {
    //private static int DATA_VERSION = 1;

    private ItemStack bottleStack = ItemStack.EMPTY;
    private long remainingTime = 0;
    private boolean wasRedstoneActive = false;
    private static final float RPM = 32f;
    private static final float BASE_STRESS_CAPACITY = 256f;

    public TimeMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide)
            return;

        boolean isRedstoneActive = level.hasNeighborSignal(worldPosition);

        // TODO clean this tick method. Redstone logic is also messy, try to clean that up into less duplicated fragments.

        // Check if redstone state changed
        if (isRedstoneActive != wasRedstoneActive) {
            wasRedstoneActive = isRedstoneActive;
            updateGeneratedRotation(); // Update when redstone state changes
            notifyUpdate();
        }

        boolean canRun =
                remainingTime > 0 &&
                !isRedstoneActive &&
                !isOverStressed();

        if (canRun) {
            remainingTime--;

            // Only send updates each second (mainly for time remaining goggle hint. notifyUpdate(); is slow)
            if (level.getGameTime() % 20 == 0) notifyUpdate();

            // Only update when time runs out (optimization)
            if (remainingTime == 0) {
                updateGeneratedRotation();
                notifyUpdate();
            }
        }
    }

    public void insertBottle(ItemStack stack) {
        if (stack.getItem() instanceof TimeInBottleItem) {
            long storedTime = TimeInBottleItem.getStoredTime(stack);
            if (storedTime > 0) {
                remainingTime += storedTime;
                TimeInBottleItem.consumeTime(stack, storedTime);
                updateGeneratedRotation();
                notifyUpdate();
            }
        }
    }

    @Override
    public float getGeneratedSpeed() {
        if (level != null && level.hasNeighborSignal(worldPosition))
            return 0;
        return remainingTime > 0 ? RPM : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        // No capacity if redstone is active OR no time remaining
        if (level != null && level.hasNeighborSignal(worldPosition))
            return 0;
        return remainingTime > 0 ? BASE_STRESS_CAPACITY : 0;
    }

    @Override
    public float calculateStressApplied() {
        return 0;   // Return 0 so the motor itself doesn't consume its own power
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        // Format time display
        long seconds = remainingTime / 20;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        String timeStr = String.format("%02d:%02d:%02d", h, m, s);

        tooltip.add(Component.literal("    Remaining Time: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(timeStr).withStyle(ChatFormatting.AQUA)));

        // Show redstone status
        if (level != null && level.hasNeighborSignal(worldPosition)) {
            tooltip.add(Component.literal("    Status: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Disabled (Redstone)").withStyle(ChatFormatting.BLUE)));
        } else if (remainingTime > 0) {
            tooltip.add(Component.literal("    Status: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Running").withStyle(ChatFormatting.GREEN)));
        } else {
            tooltip.add(Component.literal("    Status: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("No Time").withStyle(ChatFormatting.RED)));
        }

        return true;
    }

    protected void write(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket) {
        super.write(tag, provider, clientPacket);
        tag.putLong("RemainingTime", remainingTime);
        if (!bottleStack.isEmpty()) {
            tag.put("Bottle", bottleStack.save(provider));
        }
    }

    protected void read(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket) {
        super.read(tag, provider, clientPacket);
        remainingTime = tag.getLong("RemainingTime");
        if (tag.contains("Bottle")) {
            bottleStack = ItemStack.parse(provider, tag.getCompound("Bottle")).orElse(ItemStack.EMPTY);
        } else {
            bottleStack = ItemStack.EMPTY;
        }
    }
}