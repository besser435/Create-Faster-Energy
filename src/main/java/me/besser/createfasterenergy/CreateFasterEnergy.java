package me.besser.createfasterenergy;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import me.besser.createfasterenergy.blocks.FEBlockEntities;
import me.besser.createfasterenergy.blocks.FEBlocks;
import me.besser.createfasterenergy.items.FEItems;
import me.besser.createfasterenergy.util.FEConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;

@EventBusSubscriber
@Mod(CreateFasterEnergy.MODID)
public class CreateFasterEnergy {
    public static final String MODID = "fasterenergy";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {}

    public CreateFasterEnergy(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, FEConfig.SPEC);

        REGISTRATE.registerEventListeners(modEventBus);
        FEBlocks.register();
        FEBlockEntities.register();

        FEItems.ITEMS.register(modEventBus);

        LOGGER.info("Create: Faster Energy started!");
    }

    private void commonSetup(FMLCommonSetupEvent event) {}
}
