package fr.moonshade.switchrail.blocks;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class AxleCounterRail extends RailBlock implements IAxleCounterDetector {

    public AxleCounterRail() {
        super(BlockBehaviour.Properties.of(Material.DECORATION).noCollission().strength(0.7F).sound(SoundType.METAL));
        this.setRegistryName("axle_counter_rail");
    }

    @Override
    public void onMinecartPass(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {
        RailShape shape = this.getRailDirection(state,world,pos,cart);
        onMinecartPass(world,pos,cart,shape);
        super.onMinecartPass(state, world, pos, cart);
    }

    @Override
    public void onRemove(@Nonnull BlockState oldState, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState actualState, boolean p_196243_5_) {
        onRemoveAxleCounter(world,oldState,actualState,pos);
        super.onRemove(oldState, world, pos, actualState, p_196243_5_);
    }
}
