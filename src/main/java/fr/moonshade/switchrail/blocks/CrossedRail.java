package fr.moonshade.switchrail.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;


public class CrossedRail extends AbstractRailBlock implements ICrossedRail {

    private static final EnumProperty<RailShape> RAIL_STRAIGHT_FLAT;

    private RailShape fixedRailShape =RailShape.NORTH_SOUTH;

    static {
        RAIL_STRAIGHT_FLAT = EnumProperty.create("shape",RailShape.class,(railShape -> (railShape == RailShape.NORTH_SOUTH || railShape == RailShape.EAST_WEST)));
    }

    public CrossedRail() {
        super(true,Properties.of(Material.METAL)
                .noCollission()
                .lightLevel(state -> 0)
                .strength(2f)
                .sound(SoundType.METAL));
        this.setRegistryName("crossed_rail");
        this.registerDefaultState(this.stateDefinition.any().setValue(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH));
    }

    @Override
    public boolean is(ITag<Block> tag) {
        return (tag == BlockTags.RAILS);
    }

    @Override
    public boolean canMakeSlopes(BlockState p_canMakeSlopes_1_, IBlockReader p_canMakeSlopes_2_, BlockPos p_canMakeSlopes_3_) {
        return false;
    }


    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(RAIL_STRAIGHT_FLAT);
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            worldIn.setBlockAndUpdate(pos, state.setValue(RAIL_STRAIGHT_FLAT, RailShape.NORTH_SOUTH));
        }
    }

    @Override
    public RailShape getRailDirection(BlockState state, IBlockReader reader, BlockPos pos, @Nullable AbstractMinecartEntity entity) {
        if (entity != null){
            RailShape railShape = getRailShapeFromEntityAndState(pos,entity);
            if (!minecartArrivedOnBlock(entity,pos)){
                fixedRailShape=railShape;
            }
        }
        return fixedRailShape;
    }

    public boolean minecartArrivedOnBlock(AbstractMinecartEntity entity, BlockPos pos) {
        return (entity.getCommandSenderWorld().isClientSide ||
                entity.xo > pos.getX() &&
                        entity.xo < pos.getX() + 1 &&
                        entity.zo > pos.getZ() &&
                        entity.zo < pos.getZ() + 1
        );
    }

    @Override

    protected BlockState updateDir(World world, BlockPos pos, BlockState state, boolean placing) {
        return state;
    }




}
