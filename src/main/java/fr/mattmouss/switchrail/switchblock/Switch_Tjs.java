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
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class Switch_Tjs extends Switch  {

    public static EnumProperty<Corners> SWITCH_POSITION;

    private RailShape fixedRailShape =RailShape.NORTH_SOUTH;


    static  {
        SWITCH_POSITION = EnumProperty.create("switch_position",Corners.class,(corners -> {
            return (corners == Corners.TURN|| corners == Corners.STRAIGHT );
        }));
    }



    public Switch_Tjs(Properties p_i48444_2_) {
        super(true, p_i48444_2_);
        this.setRegistryName("switch_tjs");
    }

    public Switch_Tjs() {
        super(true,Properties.create(Material.IRON)
                .doesNotBlockMovement()
                .lightValue(0)
                .hardnessAndResistance(2f)
                .sound(SoundType.METAL));
        setRegistryName("switch_tjs");
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .with(SWITCH_POSITION, Corners.STRAIGHT)
                .with(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
        );
    }

    @Override
    public void updatePoweredState(World world, BlockState state, BlockPos pos, PlayerEntity player, int flags,boolean fromScreen) {
        if (!world.isRemote || fromScreen){
            Corners actualState =world.getBlockState(pos).get(SWITCH_POSITION);
            boolean isPowered = (actualState == Corners.TURN);
            if (!isPowered){
                System.out.println("changing to turn position");
                world.setBlockState(pos,
                        state.with(SWITCH_POSITION,Corners.TURN)
                        ,flags);
            }else {
                System.out.println("changing to straight position");
                world.setBlockState(pos,
                        state.with(SWITCH_POSITION,Corners.STRAIGHT)
                        ,flags);
            }
        }
    }

    @Override
    public SwitchType getType() {
        return SwitchType.TJS;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {

            world.setBlockState(pos,state
                    .with(BlockStateProperties.HORIZONTAL_FACING,getFacingFromEntity(entity, pos))
                    .with(SWITCH_POSITION,Corners.STRAIGHT)
                    .with(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH)
            );
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,
                SWITCH_POSITION,RAIL_STRAIGHT_FLAT);
    }

    @Override
    public IProperty<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }

    @Override
    public RailShape getRailDirection(BlockState state, IBlockReader reader, BlockPos pos, @Nullable AbstractMinecartEntity entity) {
        if (entity != null){
            RailShape railShape = getRailShapeFromEntityAndState(pos,entity,state);
            if (!minecartArrivedOnBlock(entity,pos)){
                fixedRailShape=railShape;
            }
        }
        return fixedRailShape;

    }

    private boolean minecartArrivedOnBlock(AbstractMinecartEntity entity, BlockPos pos) {
        return (entity.getEntityWorld().isRemote ||
                entity.prevPosX > pos.getX() &&
                        entity.prevPosX < pos.getX()+1 &&
                        entity.prevPosZ > pos.getZ() &&
                        entity.prevPosZ < pos.getZ()+1
        );

    }
    public RailShape getRailShapeFromEntityAndState(BlockPos pos, AbstractMinecartEntity entity,BlockState state) {
        Direction direction = state.get(BlockStateProperties.HORIZONTAL_FACING);
        Corners actualState = state.get(SWITCH_POSITION);
        if (entity.prevPosX<pos.getX() || entity.prevPosX> pos.getX()+1) {
            switch (direction) {
                case NORTH:
                    return (actualState == Corners.TURN) ? RailShape.SOUTH_WEST : RailShape.EAST_WEST;
                case SOUTH:
                    return (actualState == Corners.TURN) ? RailShape.NORTH_EAST : RailShape.EAST_WEST;
                case WEST:
                    return (actualState == Corners.TURN) ? RailShape.SOUTH_EAST : RailShape.EAST_WEST;
                case EAST:
                    return (actualState == Corners.TURN) ? RailShape.NORTH_WEST : RailShape.EAST_WEST;
                default:
                    throw new IllegalArgumentException("no such direction for tjs block");
            }
        } else {
            switch (direction) {
                case NORTH:
                    return (actualState == Corners.TURN) ? RailShape.SOUTH_WEST : RailShape.NORTH_SOUTH;
                case SOUTH:
                    return (actualState == Corners.TURN) ? RailShape.NORTH_EAST : RailShape.NORTH_SOUTH;
                case WEST:
                    return (actualState == Corners.TURN) ? RailShape.SOUTH_EAST : RailShape.NORTH_SOUTH;
                case EAST:
                    return (actualState == Corners.TURN) ? RailShape.NORTH_WEST : RailShape.NORTH_SOUTH;
                default:
                    throw new IllegalArgumentException("no such direction for tjs block");
            }
        }
        //  ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
    }

}
