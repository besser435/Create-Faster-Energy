package me.besser.createfasterenergy.block.charger;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class ChargerRenderer extends KineticBlockEntityRenderer<ChargerBlockEntity> {
    public ChargerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRenderOffScreen(ChargerBlockEntity be) {
        return true;
    }

    @Override
    protected BlockState getRenderedBlockState(ChargerBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }
}
