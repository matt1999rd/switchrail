package fr.mattmouss.switchrail.switchblock;

import fr.mattmouss.switchrail.enum_rail.Dss_Position;
import fr.mattmouss.switchrail.enum_rail.SwitchTypeOld;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

//check correct behaviour of method getRailShape()


public class Switch_Tjd extends Switch {

    public static EnumProperty<Dss_Position> SWITCH_POSITION;

    public static final DirectionProperty FACING_AXE = DirectionProperty.create("facing_axe", Direction.NORTH,Direction.EAST);

    static  {
        SWITCH_POSITION=EnumProperty.create("switch_position", Dss_Position.class);
    }

    private RailShape fixedRailShape= RailShape.NORTH_SOUTH;

    //RailShape.NORTH_SOUTH classic model
    //RailShape.EAST_WEST model with West replacing North

    public Switch_Tjd() {
        super(Properties.of(Material.METAL)
                .noCollission()
                .lightLevel(state -> 0)
                .strength(2f)
                .sound(SoundType.METAL));
        this.setRegistryName("switch_tjd");
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SWITCH_POSITION, Dss_Position.NO_POWER)
                .setValue(FACING_AXE,Direction.NORTH)
                .setValue(RAIL_STRAIGHT_FLAT, RailShape.NORTH_SOUTH)
        );
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {
            world.setBlockAndUpdate(pos,state
                    .setValue(SWITCH_POSITION, Dss_Position.NO_POWER)
                    .setValue(FACING_AXE,getFacingFromEntity(entity, pos))
                    .setValue(RAIL_STRAIGHT_FLAT, RailShape.NORTH_SOUTH)
            );
        }
    }

    private Direction getFacingFromEntity(LivingEntity entity,BlockPos pos) {
        Direction d = Util.getFacingFromEntity(entity, pos,true);
        switch (d) {
            case NORTH:
            case SOUTH:
                return Direction.NORTH;
            case EAST:
            case WEST:
                return Direction.EAST;
            default:
                throw new IllegalArgumentException("no position up or down in this scope");
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(SWITCH_POSITION,FACING_AXE,RAIL_STRAIGHT_FLAT);
        super.createBlockStateDefinition(builder);
    }
    //blockPos convert doubles to integers
    @Override
    public void updatePoweredState(World world, BlockState state, BlockPos pos, PlayerEntity player, int flags,boolean fromScreen) {
        double[] pos_ru_sw;
        double[] pos_ld_sw;
        pos_ru_sw=(state.getValue(FACING_AXE) == Direction.NORTH)?
                new double[]{pos.getX() + 0.9, pos.getY() + 0.1, pos.getZ() + 0.1}:
                new double[]{pos.getX() + 0.9, pos.getY() + 0.1, pos.getZ() + 0.9};
        pos_ld_sw = (state.getValue(FACING_AXE) == Direction.NORTH)?
                new double[]{pos.getX() + 0.1, pos.getY() + 0.1, pos.getZ() + 0.9}:
                new double[]{pos.getX() + 0.1, pos.getY() + 0.1, pos.getZ() + 0.1};
        //determine the theoretic position of the small voxel constituting the switch in order to change the nearest one
        if (!world.isClientSide || fromScreen) {
            Vector3d vec3d = player.position();
            double[] player_pos = {vec3d.x, vec3d.y, vec3d.z};
            System.out.println("distance with the switch right up :" + Distance3(pos_ru_sw, player_pos));
            System.out.println("distance with the switch left down :" + Distance3(pos_ld_sw, player_pos));
            System.out.println("nearest switch :" +
                    ((Distance3(pos_ru_sw, player_pos) > Distance3(pos_ld_sw, player_pos)) ?
                            "switch left down" :
                            "switch up right"));

            Dss_Position actualState = world.getBlockState(pos).getValue(SWITCH_POSITION);

            updatePowerState(world,state,pos,flags,
                    Distance3(pos_ru_sw, player_pos) > Distance3(pos_ld_sw, player_pos)
                    ,actualState);
        }
    }

    @Override
    public EnumProperty<?> getSwitchPositionProperty() {
        return SWITCH_POSITION;
    }

    public void updatePowerState(World world, BlockState state, BlockPos pos, int flags, boolean ld_nearest, Dss_Position actualState){
        world.setBlock(pos,state.setValue(SWITCH_POSITION,actualState.moveDssSwitch(ld_nearest)),flags);
    }


    private double Distance3(double[] first_pos, double[] player_pos) {

        return MathHelper.sqrt(
                Math.pow(first_pos[0]-player_pos[0],2.0)+
               Math.pow(first_pos[1]-player_pos[1],2.0)+
               Math.pow(first_pos[2]-player_pos[2],2.0)) ;

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
                        entity.xo < pos.getX()+1 &&
                        entity.zo > pos.getZ() &&
                        entity.zo < pos.getZ()+1
        );
    }


    public RailShape getRailShapeFromEntityAndState(BlockPos pos, AbstractMinecartEntity entity) {
        Dss_Position tjd_position = entity.getCommandSenderWorld().getBlockState(pos).getValue(SWITCH_POSITION);
        Direction tjd_dir = entity.getCommandSenderWorld().getBlockState(pos).getValue(FACING_AXE);

        //minecart come from west
        if (entity.xo<pos.getX()){
            switch (tjd_position) {
                case NO_POWER:
                    return RailShape.EAST_WEST ;
                case POWER_ON_RIGHT_UP:
                case POWER_ON_BOTH:
                    return (tjd_dir == Direction.NORTH) ? RailShape.SOUTH_WEST : RailShape.NORTH_WEST;
                case POWER_ON_LEFT_DOWN:
                    return (tjd_dir == Direction.NORTH) ? RailShape.NORTH_EAST : RailShape.SOUTH_EAST;
                default:
                    throw new IllegalArgumentException("no such direction for tjs block");
            }
        //minecart come from east
        }else if (entity.xo> pos.getX()+1) {
            switch (tjd_position) {
                case NO_POWER:
                    return RailShape.EAST_WEST ;
                case POWER_ON_RIGHT_UP:
                    return (tjd_dir == Direction.NORTH) ? RailShape.SOUTH_WEST : RailShape.NORTH_WEST;
                case POWER_ON_LEFT_DOWN:
                case POWER_ON_BOTH:
                    return (tjd_dir == Direction.NORTH) ? RailShape.NORTH_EAST : RailShape.SOUTH_EAST;
                default:
                    throw new IllegalArgumentException("no such direction for tjs block");
            }
        //minecart come from north
        }else if (entity.zo<pos.getZ()) {
            switch (tjd_position) {
                case NO_POWER:
                    return RailShape.NORTH_SOUTH ;
                case POWER_ON_RIGHT_UP:
                    return (tjd_dir == Direction.NORTH) ? RailShape.SOUTH_WEST : RailShape.NORTH_WEST;
                case POWER_ON_LEFT_DOWN:
                    return (tjd_dir == Direction.NORTH) ? RailShape.NORTH_EAST : RailShape.SOUTH_EAST;
                case POWER_ON_BOTH:
                    return (tjd_dir == Direction.NORTH) ? RailShape.NORTH_EAST : RailShape.NORTH_WEST;
                default:
                    throw new IllegalArgumentException("no such direction for tjs block");
            }
        //minecart come from south
        }else {
            switch (tjd_position) {
                case NO_POWER:
                    return RailShape.NORTH_SOUTH ;
                case POWER_ON_RIGHT_UP:
                    return (tjd_dir == Direction.NORTH) ? RailShape.SOUTH_WEST : RailShape.NORTH_WEST;
                case POWER_ON_LEFT_DOWN:
                    return (tjd_dir == Direction.NORTH) ? RailShape.NORTH_EAST : RailShape.SOUTH_EAST;
                case POWER_ON_BOTH:
                    return (tjd_dir == Direction.NORTH) ? RailShape.SOUTH_WEST : RailShape.SOUTH_EAST;
                default:
                    throw new IllegalArgumentException("no such direction for tjs block");
            }

        }
    }
}
