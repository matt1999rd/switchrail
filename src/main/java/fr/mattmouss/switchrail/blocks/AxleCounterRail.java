package fr.mattmouss.switchrail.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class AxleCounterRail extends RailBlock implements IAxleCounterDetector {

    public AxleCounterRail() {
        super(AbstractBlock.Properties.of(Material.DECORATION).noCollission().strength(0.7F).sound(SoundType.METAL));
        this.setRegistryName("axle_counter_rail");
    }

    @Override
    public void onMinecartPass(BlockState state, World world, BlockPos pos, AbstractMinecartEntity cart) {
        RailShape shape = this.getRailDirection(state,world,pos,cart);
        onMinecartPass(world,pos,cart,shape);
        super.onMinecartPass(state, world, pos, cart);
    }

    @Override
    public boolean is(@Nonnull ITag<Block> tag) {
        return tag == BlockTags.RAILS;
    }

    @Override
    public void onRemove(BlockState oldState, World world, BlockPos pos, BlockState actualState, boolean p_196243_5_) {
        removeCP(world,oldState,actualState,pos);
        super.onRemove(oldState, world, pos, actualState, p_196243_5_);
    }
}
