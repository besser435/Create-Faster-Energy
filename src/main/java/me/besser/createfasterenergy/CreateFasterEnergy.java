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
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "fasterenergy" namespace
    //public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    // Creates a creative tab with the id "fasterenergy:example_tab" for the example item, that is placed after the combat tab
//    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
//            .title(Component.translatable("itemGroup.fasterenergy")) //The language key for the title of your CreativeModeTab
//            .withTabsBefore(CreativeModeTabs.COMBAT)
//            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
//            .displayItems((parameters, output) -> {
//                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
//            }).build());

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {}

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CreateFasterEnergy(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, FEConfig.SPEC);

        // Register the item to a creative tab
        // modEventBus.addListener(this::addCreative);

        REGISTRATE.registerEventListeners(modEventBus);
        FEBlocks.register();
        FEBlockEntities.register();

        FEItems.ITEMS.register(modEventBus);

        LOGGER.info("Create: Faster Energy started!");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Any non-registration stuff should go here
    }

    // Add the example block item to the building blocks tab
//    private void addCreative(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
//            event.accept(EXAMPLE_BLOCK_ITEM);
//        }
//    }
}
