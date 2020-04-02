package fr.mattmouss.switchrail.blocks;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class CrossedRail extends AbstractRailBlock {

    private static EnumProperty<RailShape> RAIL_STRAIGHT_FLAT;

    private RailShape fixedRailShape =RailShape.NORTH_SOUTH;

    static {
        RAIL_STRAIGHT_FLAT = EnumProperty.create("shape",RailShape.class,(railShape -> {
            return (railShape == RailShape.NORTH_SOUTH || railShape == RailShape.EAST_WEST);
        }));
    }

    public CrossedRail() {
        super(true,Properties.create(Material.IRON)
                .doesNotBlockMovement()
                .lightValue(0)
                .hardnessAndResistance(2f)
                .sound(SoundType.METAL));
        this.setRegistryName("crossed_rail");
        this.setDefaultState(this.stateContainer.getBaseState().with(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH));
    }

    @Override
    public boolean isIn(Tag<Block> tag) {
        return (tag == BlockTags.RAILS);
    }

    @Override
    public boolean canMakeSlopes(BlockState p_canMakeSlopes_1_, IBlockReader p_canMakeSlopes_2_, BlockPos p_canMakeSlopes_3_) {
        return false;
    }


    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(RAIL_STRAIGHT_FLAT);
    }

    @Override
    public IProperty<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }

    @Override
    public void onBlockPlacedBy(World p_180633_1_, BlockPos p_180633_2_, BlockState p_180633_3_, @Nullable LivingEntity p_180633_4_, ItemStack p_180633_5_) {
        if (p_180633_4_ != null) {
            p_180633_1_.setBlockState(p_180633_2_, p_180633_3_.with(RAIL_STRAIGHT_FLAT, RailShape.NORTH_SOUTH));
        }
    }

    @Override
    public RailShape getRailDirection(BlockState state, IBlockReader reader, BlockPos pos, @Nullable AbstractMinecartEntity entity) {
        if (entity != null){
            RailShape railShape = getRailShapeFromEntityAndState(pos,entity,null);
            if (!minecartArrivedOnBlock(entity,pos)){
                fixedRailShape=railShape;
            }
        }
        return fixedRailShape;
    }

    public boolean minecartArrivedOnBlock(AbstractMinecartEntity entity, BlockPos pos) {
        return (entity.getEntityWorld().isRemote ||
                entity.prevPosX > pos.getX() &&
                        entity.prevPosX < pos.getX()+1 &&
                        entity.prevPosZ > pos.getZ() &&
                        entity.prevPosZ < pos.getZ()+1
        );

    }

    public RailShape getRailShapeFromEntityAndState(BlockPos pos, AbstractMinecartEntity entity, BlockState state) {
        return (entity.prevPosX<pos.getX() || entity.prevPosX> pos.getX()+1) ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
    }



    @Override

    protected BlockState getUpdatedState(World world, BlockPos p_208489_2_, BlockState state, boolean p_208489_4_) {
        return state;
    }




}
