package me.besser.createfasterenergy.blocks.time_motor;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import me.besser.createfasterenergy.blocks.FEBlockEntities;
import me.besser.createfasterenergy.items.TimeInBottleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;


public class TimeMotorBlock extends DirectionalKineticBlock implements IBE<TimeMotorBlockEntity> {
    public TimeMotorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        // This allows shafts to connect to both the front and the back of the motor
        // Keep the both sides. allows people to stack them easier.
        return face.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if ((context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown()) || preferred == null)
            return super.getStateForPlacement(context);
        return defaultBlockState().setValue(FACING, preferred);
    }

    @Override
    public Class<TimeMotorBlockEntity> getBlockEntityClass() {
        return TimeMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TimeMotorBlockEntity> getBlockEntityType() {
        return FEBlockEntities.TIME_MOTOR.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;

        if (stack.getItem() instanceof TimeInBottleItem) {
            if (level.getBlockEntity(pos) instanceof TimeMotorBlockEntity motor) {
                long storedTime = TimeInBottleItem.getStoredTime(stack);
                if (storedTime > 0) {
                    motor.insertBottle(stack);
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}