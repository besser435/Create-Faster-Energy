package me.besser.createfasterenergy.ponder;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import me.besser.createfasterenergy.CreateFasterEnergy;
import me.besser.createfasterenergy.blocks.FEBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FEPonderPlugin implements PonderPlugin {
    public FEPonderPlugin() {
        PonderIndex.addPlugin(this);
    }

    @Override
    public @NotNull String getModId() {
        return CreateFasterEnergy.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(FEBlocks.ALTERNATOR.getId())
                .addStoryBoard("alternator_ponder",
                        PonderScenes::alternator,
                        AllCreatePonderTags.KINETIC_SOURCES);
    }
}
