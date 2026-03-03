package me.besser.createfasterenergy.mixin;

import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.mounted.MountedContraption;
import me.besser.createfasterenergy.CreateFasterEnergy;
import me.besser.createfasterenergy.util.ContraptionLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// NOTE: This only affects Minecart Contraptions
@Mixin(value = MountedContraption.class, remap = false)
public class ContraptionMixin {

    @Inject(
            method = "assemble",
            at = @At("RETURN")
    )
    private void onAssemble(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) throws AssemblyException {
        //CreateFasterEnergy.LOGGER.info("in mixin\n\n\n\n\n");

        if (cir.getReturnValue()) {
            //CreateFasterEnergy.LOGGER.info("assembled\n\n\n\n\n");

            Contraption contraption = (Contraption) (Object) this;  // Cast MountedContraption to general Contraption
            ContraptionLimiter.validate(contraption, pos);
        }
    }
}