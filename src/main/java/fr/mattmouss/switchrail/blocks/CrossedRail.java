package fr.mattmouss.switchrail.blocks;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;


import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class CrossedRail extends BaseRailBlock implements ICrossedRail {

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
    public boolean canMakeSlopes(BlockState p_canMakeSlopes_1_, BlockGetter p_canMakeSlopes_2_, BlockPos p_canMakeSlopes_3_) {
        return false;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RAIL_STRAIGHT_FLAT,WATERLOGGED);
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            worldIn.setBlockAndUpdate(pos, state.setValue(RAIL_STRAIGHT_FLAT, RailShape.NORTH_SOUTH));
        }
    }

    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter reader, BlockPos pos, @Nullable AbstractMinecart entity) {
        if (entity != null){
            RailShape railShape = getRailShapeFromEntityAndState(pos,entity);
            if (!minecartArrivedOnBlock(entity,pos)){
                fixedRailShape=railShape;
            }
        }
        return fixedRailShape;
    }

    public boolean minecartArrivedOnBlock(AbstractMinecart entity, BlockPos pos) {
        return (entity.getCommandSenderWorld().isClientSide ||
                entity.xo > pos.getX() &&
                        entity.xo < pos.getX() + 1 &&
                        entity.zo > pos.getZ() &&
                        entity.zo < pos.getZ() + 1
        );
    }

    @Override

    protected BlockState updateDir(Level world, BlockPos pos, BlockState state, boolean placing) {
        return state;
    }




}
