package me.besser.createfasterenergy.items;

import me.besser.createfasterenergy.CreateFasterEnergy;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FEItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateFasterEnergy.MODID);

    public static final DeferredItem<Item> TIME_IN_BOTTLE = ITEMS.register("time_in_bottle",
            () -> new TimeInBottleItem(new Item.Properties().stacksTo(1)));
}