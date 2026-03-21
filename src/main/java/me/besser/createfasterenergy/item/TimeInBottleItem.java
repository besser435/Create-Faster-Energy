package me.besser.createfasterenergy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class TimeInBottleItem extends Item {
    public TimeInBottleItem(Properties props) {
        super(props);
    }

    private static final String STORED_TIME = "StoredTime";
    private static final long MAX_TIME = 20L * 60 * 60 * 12; // 12h


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) return;

        // Only run once per second
        if ((level.getGameTime() % 20) != 0) return;

        // 80% efficiency
        if (level.getRandom().nextFloat() > 0.8f) return;

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data != null ? data.copyTag() : new CompoundTag();

        // To nerf it, and keep it safe from huge numbers
        long current = tag.getLong(STORED_TIME);
        if (current >= MAX_TIME)
            return;

        long newTime = Math.min(current + 20, MAX_TIME);
        tag.putLong(STORED_TIME, newTime);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        long ticks = getStoredTime(stack);
        tooltip.add(Component.literal("Stored Time: ").withStyle(ChatFormatting.GRAY)
                .append(formatTime(ticks)).withStyle(ChatFormatting.AQUA));

        tooltip.add(Component.literal("Efficiency: 80%").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    private String formatTime(long ticks) {
        long seconds = ticks / 20;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    // Prevent bobbing when the NBT updates
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged; // Only animate if the item moved slots, not if data changed
    }

    // Time stored progress bar
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * ((float) getStoredTime(stack) / MAX_TIME));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xEFEFEF;
    }

    // Helpers
    public static long getStoredTime(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null)
            return 0;

        return data.copyTag().getLong(STORED_TIME);
    }

    public static void consumeTime(ItemStack stack, long amount) {
        if (amount <= 0)
            return;

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null)
            return;

        CompoundTag tag = data.copyTag();
        long current = tag.getLong(STORED_TIME);

        if (current <= 0)
            return;

        long newValue = Math.max(0, current - amount);
        if (newValue == current)
            return;

        tag.putLong(STORED_TIME, newValue);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
