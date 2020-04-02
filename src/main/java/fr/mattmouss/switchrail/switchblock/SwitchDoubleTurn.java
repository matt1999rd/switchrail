package fr.mattmouss.switchrail.switchblock;

import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.enum_rail.SwitchType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class SwitchDoubleTurn extends Switch {

    public static EnumProperty<Corners> SWITCH_POSITION;



    static  {
        SWITCH_POSITION = EnumProperty.create("switch_position",Corners.class,(corners -> {
            return (corners == Corners.TURN_LEFT|| corners == Corners.TURN_RIGHT );
        }));
    }


    public SwitchDoubleTurn(Properties builder) {
        super(true,builder);
        setRegistryName("double_turn_switch");
    }


    public SwitchDoubleTurn() {
        super(true,Properties.create(Material.IRON)
        .doesNotBlockMovement()
        .lightValue(0)
        .hardnessAndResistance(2f)
        .sound(SoundType.METAL));
        setRegistryName("double_turn_switch");
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH)
                .with(SWITCH_POSITION,Corners.TURN_LEFT)
        );

    }




    @Override
    public boolean canMakeSlopes(BlockState p_canMakeSlopes_1_, IBlockReader p_canMakeSlopes_2_, BlockPos p_canMakeSlopes_3_) {
        return false;
    }

    @Override
    public boolean isValidPosition(BlockState p_196260_1_, IWorldReader p_196260_2_, BlockPos p_196260_3_) {
        return true;
    }




    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {

            world.setBlockState(pos,state
                    .with(BlockStateProperties.HORIZONTAL_FACING,getFacingFromEntity(entity, pos))
                    .with(SWITCH_POSITION,Corners.TURN_LEFT)
                    .with(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
            );
        }
    }


    @Override
    public void fillStateContainer(StateContainer.Builder<Block,BlockState> builder){
        builder.add(BlockStateProperties.HORIZONTAL_FACING,
                SWITCH_POSITION,
                RAIL_STRAIGHT_FLAT);
    }


    public void updatePoweredState(World world, BlockState state, BlockPos pos, PlayerEntity player, int flags,boolean fromScreen) {
        if (!world.isRemote || fromScreen){
            Corners actualState = world.getBlockState(pos).get(SWITCH_POSITION);
            boolean isPowered = (actualState == Corners.TURN_RIGHT);
            System.out.println("valeur de state.get(...): "+state.get(SWITCH_POSITION));
            Direction currentDirection=world.getBlockState(pos).get(BlockStateProperties.HORIZONTAL_FACING);
            if (!isPowered){
                System.out.println("changing to turn_right position");
                world.setBlockState(pos,state.with(SWITCH_POSITION,Corners.TURN_RIGHT).with(BlockStateProperties.HORIZONTAL_FACING,currentDirection),flags);
                actualState = Corners.TURN_RIGHT;
            }else {
                System.out.println("changing to turn_left position");
                world.setBlockState(pos,state.with(SWITCH_POSITION,Corners.TURN_LEFT).with(BlockStateProperties.HORIZONTAL_FACING,currentDirection),flags);
                actualState = Corners.TURN_LEFT;
            }
        }
    }

    @Override
    public SwitchType getType() {
        return SwitchType.DOUBLE_TURN;
    }

    @Override
    public RailShape getRailDirection(BlockState state, IBlockReader reader, BlockPos pos, @Nullable AbstractMinecartEntity minecartEntity) {
        Direction direction = state.get(BlockStateProperties.HORIZONTAL_FACING);
        Corners actualState = state.get(SWITCH_POSITION);

        switch (direction) {
            case NORTH:
                return (actualState == Corners.TURN_LEFT)? RailShape.SOUTH_WEST : RailShape.SOUTH_EAST;
            case SOUTH:
                return (actualState == Corners.TURN_LEFT)? RailShape.NORTH_EAST : RailShape.NORTH_WEST;
            case WEST:
                return (actualState == Corners.TURN_LEFT)? RailShape.SOUTH_EAST : RailShape.NORTH_EAST;
            case EAST:
                return (actualState == Corners.TURN_LEFT)? RailShape.NORTH_WEST : RailShape.SOUTH_WEST;
            default:
                throw new IllegalArgumentException("no such direction for double turn block");
        }
    }

    @Override
    public IProperty<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }



}
