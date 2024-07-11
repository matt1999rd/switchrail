package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.other.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;



public class OneWayPoweredRail extends PoweredRailBlock {

    private static final BooleanProperty FIRST_WAY_POW;
    static {
        FIRST_WAY_POW = BooleanProperty.create("first_direction");
    }


    public OneWayPoweredRail() {
        super(Properties.of(Material.DECORATION)
                .noCollission()
                .lightLevel(state -> 0)
                .strength(0.7f)
                .sound(SoundType.METAL),true);
        this.setRegistryName("one_way_powered_rail");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FIRST_WAY_POW);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            Direction dir = Util.getDirectionFromEntity(placer,pos,false);
            RailShape shape = (dir.getAxis() == Direction.Axis.X) ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
            boolean firstWayPowered =
                    ((dir == Direction.SOUTH) && shape == RailShape.NORTH_SOUTH) ||
                            ((dir == Direction.WEST) && shape == RailShape.EAST_WEST);
            worldIn.setBlockAndUpdate(pos,state
                    .setValue(FIRST_WAY_POW, firstWayPowered)
                    .setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT,shape));
        }
    }

    //get the direction where the rail is powered
    private Direction getDirectionFromState(RailShape shape,boolean firstWayPowered){
        if (shape == RailShape.EAST_WEST || shape == RailShape.ASCENDING_EAST || shape == RailShape.ASCENDING_WEST){
            return (firstWayPowered)? Direction.EAST : Direction.WEST;
        }else {
            return (firstWayPowered)? Direction.NORTH : Direction.SOUTH;
        }
    }

    @Override
    public void onMinecartPass(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {
        RailShape shape = state.getValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT);
        boolean firstWayPowered = state.getValue(FIRST_WAY_POW);
        Direction d = getDirectionFromState(shape,firstWayPowered);
        boolean isActivated = state.getValue(BlockStateProperties.POWERED);
        Vec3 vec3d = cart.getDeltaMovement();
        //done if the block is powered
        if (isActivated){
            double initialSpeed = 0.02D;
            //if the train is on the rail and is stopped we initialise its speed
            // (like powered rail when one of the two direction is blocked with
            // an opaque cube)

            if (vec3d.x == 0 && vec3d.z == 0){
                Vec3 newMotion;
                System.out.println("instantiating speed");
                switch (d){
                    case EAST:
                        newMotion = new Vec3(-initialSpeed,0.0D,0.0D);
                        break;
                    case SOUTH:
                        newMotion = new Vec3(0.0D,0.0D,-initialSpeed);
                        break;
                    case UP:
                    case DOWN:
                    default:
                        throw new IllegalArgumentException("no such direction authorised : "+d);
                    case WEST:
                        newMotion = new Vec3(initialSpeed,0.0D,0.0D);
                        break;
                    case NORTH:
                        newMotion = new Vec3(0.0D,0.0D,initialSpeed);
                }
                cart.setDeltaMovement(newMotion);
            }
            //done if the train is moving it is also done for instantiate
            // block, but it will automatically be verified
            // as for other cart if it is the right direction it will accelerate it
            Direction motionDirection = Direction.getNearest(vec3d.x,vec3d.y,vec3d.z);
            if (motionDirection == d){
                //direction match so the train is accelerated
                Vec3 vec3d2 = cart.getDeltaMovement();
                double horMag = Math.sqrt(vec3d2.horizontalDistanceSqr());
                double gain = 0.06D;
                cart.setDeltaMovement(vec3d2.add(new Vec3(vec3d2.x/horMag * gain,0.0D, vec3d2.z/horMag * gain)));
                System.out.println("accelerating train");
            }
            else {
                //if it is rolling on the other direction we need to reverse its speed
                Vec3 vec3d1 = cart.getDeltaMovement();
                cart.setDeltaMovement(vec3d1.reverse());
            }
        }else {
            // if the rail is not powered it will instantly stop the rail and fix it on a position
            cart.setDeltaMovement(Vec3.ZERO);
            cart.lerpMotion(0,0,0);
            if (cart.xo < pos.getX()+0.55 && cart.xo> pos.getX()+0.45){
                System.out.println("fixed cart !!");
                cart.setPos(cart.xo,cart.yo,cart.zo);
            }
        }
    }

}
