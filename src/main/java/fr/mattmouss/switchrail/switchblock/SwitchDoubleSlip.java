package fr.mattmouss.switchrail.switchblock;

import fr.mattmouss.switchrail.blocks.CrossedRail;
import fr.mattmouss.switchrail.blocks.ICrossedRail;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.lwjgl.system.CallbackI;

import javax.annotation.Nullable;

//check correct behaviour of method getRailShape()


public class SwitchDoubleSlip extends Switch implements ICrossedRail {

    private RailShape fixedRailShape= RailShape.NORTH_SOUTH;

    //RailShape.NORTH_SOUTH classic model
    //RailShape.EAST_WEST model with West replacing North

    public SwitchDoubleSlip() {
        super(Properties.of(Material.METAL)
                .noCollission()
                .lightLevel(state -> 0)
                .strength(2f)
                .sound(SoundType.METAL));
        this.setRegistryName("switch_tjd");
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(DSS_POSITION, Corners.STRAIGHT)
                .setValue(RAIL_STRAIGHT_FLAT, RailShape.NORTH_SOUTH)
        );
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {
            world.setBlockAndUpdate(pos,state
                    .setValue(DSS_POSITION, Corners.STRAIGHT)
                    .setValue(BlockStateProperties.HORIZONTAL_AXIS,getFacingFromEntity(entity, pos))
                    .setValue(RAIL_STRAIGHT_FLAT, RailShape.NORTH_SOUTH)
            );
        }
    }

    private Direction.Axis getFacingFromEntity(LivingEntity entity,BlockPos pos) {
        Direction d = Util.getFacingFromEntity(entity, pos,true);
        return d.getAxis();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(RAIL_STRAIGHT_FLAT,DSS_POSITION, BlockStateProperties.HORIZONTAL_AXIS);
        super.createBlockStateDefinition(builder);
    }

    //blockPos convert doubles to integers
    @Override
    public void updatePoweredState(World world, BlockState state, BlockPos pos, PlayerEntity player, int flags,boolean fromScreen) {
        double[] pos_ru_sw;
        double[] pos_ld_sw;
        pos_ru_sw=(state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.Z)?
                new double[]{pos.getX() + 0.9, pos.getY() + 0.1, pos.getZ() + 0.1}:
                new double[]{pos.getX() + 0.9, pos.getY() + 0.1, pos.getZ() + 0.9};
        pos_ld_sw = (state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.Z)?
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

            Corners actualState = world.getBlockState(pos).getValue(DSS_POSITION);
            updatePowerState(world,state,pos,flags,
                    Distance3(pos_ru_sw, player_pos) > Distance3(pos_ld_sw, player_pos)
                    ,actualState);
        }
    }

    @Override
    public EnumProperty<Corners> getSwitchPositionProperty() {
        return DSS_POSITION;
    }

    public void updatePowerState(World world, BlockState state, BlockPos pos, int flags, boolean ld_nearest, Corners actualState){
        world.setBlock(pos,state.setValue(DSS_POSITION,actualState.moveDssSwitch(ld_nearest)),flags);
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
        Corners tjd_position = entity.getCommandSenderWorld().getBlockState(pos).getValue(DSS_POSITION);
        Direction.Axis tjd_axis = entity.getCommandSenderWorld().getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_AXIS);
        if (tjd_position == Corners.STRAIGHT){
            return ICrossedRail.super.getRailShapeFromEntityAndState(pos,entity);
        }else if (tjd_position == Corners.TURN_LEFT){
            return (tjd_axis == Direction.Axis.Z) ? RailShape.SOUTH_WEST : RailShape.NORTH_WEST;
        }else if (tjd_position == Corners.TURN_RIGHT){
            return (tjd_axis == Direction.Axis.Z) ? RailShape.NORTH_EAST : RailShape.SOUTH_EAST;
        }
        Direction pointDirection = getMinecartComingDirection(entity,pos);
        return Util.getShapeFromDirection(pointDirection,
                tjd_position.getHeelDirection(
                        pointDirection,
                        (tjd_axis == Direction.Axis.Z? DoorHingeSide.LEFT : DoorHingeSide.RIGHT)));
    }

    private Direction getMinecartComingDirection(AbstractMinecartEntity minecart,BlockPos pos){
        if (minecart.xo<pos.getX()){
            return Direction.EAST;
        }else if (minecart.xo> pos.getX()+1){
            return Direction.WEST;
        }else if (minecart.zo< pos.getZ()){
            return Direction.NORTH;
        }else if (minecart.zo> pos.getZ()+1){
            return Direction.SOUTH;
        }
        return null;
    }
}
