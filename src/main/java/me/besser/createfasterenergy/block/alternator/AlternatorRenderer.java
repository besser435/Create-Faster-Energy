package me.besser.createfasterenergy.block.alternator;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class AlternatorRenderer extends KineticBlockEntityRenderer<AlternatorBlockEntity> {
    public AlternatorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull AlternatorBlockEntity be) {
        return true;
    }

    @Override
    protected BlockState getRenderedBlockState(AlternatorBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }
}
