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
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class SwitchTriple extends Switch {

    public SwitchTriple(boolean allow_corners, Properties properties) {
        super(true, properties);
        this.setRegistryName("triple_switch");
    }

    public SwitchTriple() {
        super(true,Properties.create(Material.IRON)
                .doesNotBlockMovement()
                .lightValue(0)
                .hardnessAndResistance(2f)
                .sound(SoundType.METAL));
        this.setRegistryName("triple_switch");
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .with(SWITCH_POSITION_TRIPLE,Corners.TURN_LEFT)
                .with(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
        );
    }


    @Override
    public void updatePoweredState(World world, BlockState state, BlockPos pos, PlayerEntity player, int flags,boolean fromScreen) {
        if (!world.isRemote || fromScreen){
            Corners actualState = world.getBlockState(pos).get(SWITCH_POSITION_TRIPLE);
            int caract_state = (actualState == Corners.TURN_RIGHT) ? 2 :
                    (actualState ==Corners.STRAIGHT) ? 1 : 0;
            Direction currentDirection=world.getBlockState(pos).get(BlockStateProperties.HORIZONTAL_FACING);
            if (caract_state == 0){
                System.out.println("changing to straight position");
                world.setBlockState(pos,state.with(SWITCH_POSITION_TRIPLE,Corners.STRAIGHT).with(BlockStateProperties.HORIZONTAL_FACING,currentDirection),flags);
            } else if (caract_state == 1){
                System.out.println("changing to turn_right position");
                world.setBlockState(pos,state.with(SWITCH_POSITION_TRIPLE,Corners.TURN_RIGHT).with(BlockStateProperties.HORIZONTAL_FACING,currentDirection),flags);
            } else {
                System.out.println("changing to turn_left position");
                world.setBlockState(pos,state.with(SWITCH_POSITION_TRIPLE,Corners.TURN_LEFT).with(BlockStateProperties.HORIZONTAL_FACING,currentDirection),flags);
            }
        }

    }

    @Override
    public SwitchType getType() {
        return SwitchType.TRIPLE;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {
            world.setBlockState(pos,state
                    .with(BlockStateProperties.HORIZONTAL_FACING,getFacingFromEntity(entity, pos))
                    .with(SWITCH_POSITION_TRIPLE,Corners.TURN_LEFT)
                    .with(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
            );

        }
    }

    @Override
    public void fillStateContainer(StateContainer.Builder<Block,BlockState> builder){
        builder.add(BlockStateProperties.HORIZONTAL_FACING,
                SWITCH_POSITION_TRIPLE,RAIL_STRAIGHT_FLAT);
    }

    @Override
    public RailShape getRailDirection(BlockState state, IBlockReader reader, BlockPos pos, @Nullable AbstractMinecartEntity entity) {
        switch (state.get(BlockStateProperties.HORIZONTAL_FACING)){
            case EAST:
                switch (state.get(SWITCH_POSITION_TRIPLE)) {
                    case TURN_LEFT:
                        return RailShape.NORTH_WEST;
                    case STRAIGHT:
                        return RailShape.EAST_WEST;
                    case TURN_RIGHT:
                        return RailShape.SOUTH_WEST;
                    default:
                        throw new IllegalArgumentException("no such direction for triple switch block");
                }
            case WEST:
                switch (state.get(SWITCH_POSITION_TRIPLE)) {
                    case TURN_LEFT:
                        return RailShape.SOUTH_EAST;
                    case STRAIGHT:
                        return RailShape.EAST_WEST;
                    case TURN_RIGHT:
                        return RailShape.NORTH_EAST;
                    default:
                        throw new IllegalArgumentException("no such direction for triple switch block");
                }
            case NORTH:
                switch (state.get(SWITCH_POSITION_TRIPLE)) {
                    case TURN_LEFT:
                        return RailShape.SOUTH_WEST;
                    case STRAIGHT:
                        return RailShape.NORTH_SOUTH;
                    case TURN_RIGHT:
                        return RailShape.SOUTH_EAST;
                    default:
                        throw new IllegalArgumentException("no such direction for triple switch block");
                }
            case SOUTH:
                switch (state.get(SWITCH_POSITION_TRIPLE)) {
                    case TURN_LEFT:
                        return RailShape.NORTH_WEST;
                    case STRAIGHT:
                        return RailShape.NORTH_SOUTH;
                    case TURN_RIGHT:
                        return RailShape.NORTH_EAST;
                    default:
                        throw new IllegalArgumentException("no such direction for triple switch block");
                }
            default:
                throw new IllegalArgumentException("no such direction for triple switch block");
        }
    }



    @Override
    public IProperty<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }
}
