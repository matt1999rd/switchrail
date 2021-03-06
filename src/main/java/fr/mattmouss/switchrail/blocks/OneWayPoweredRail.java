package fr.mattmouss.switchrail.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.lwjgl.system.CallbackI;

import javax.annotation.Nullable;

import static net.minecraft.entity.Entity.horizontalMag;

public class OneWayPoweredRail extends PoweredRailBlock {

    private static BooleanProperty FIRST_WAY_POW;
    static {
        FIRST_WAY_POW = BooleanProperty.create("first_direction");
    }


    public OneWayPoweredRail() {
        super(Properties.create(Material.MISCELLANEOUS)
                .doesNotBlockMovement()
                .lightValue(0)
                .hardnessAndResistance(0.7f)
                .sound(SoundType.METAL),true);
        this.setRegistryName("one_way_powered_rail");
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FIRST_WAY_POW);
        super.fillStateContainer(builder);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            Direction dir = getDirectionFromEntity(placer,pos);
            RailShape shape = (dir.getAxis() == Direction.Axis.X) ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
            boolean firstWayPowered =
                    ((dir == Direction.SOUTH) && shape == RailShape.NORTH_SOUTH) ||
                            ((dir == Direction.WEST) && shape == RailShape.EAST_WEST);
            worldIn.setBlockState(pos,state
                    .with(FIRST_WAY_POW, firstWayPowered)
                    .with(BlockStateProperties.RAIL_SHAPE_STRAIGHT,shape));
        }
    }

    @Override
    public boolean isIn(Tag<Block> tag) {
        return (tag == BlockTags.RAILS);
    }

    private Direction getDirectionFromEntity(LivingEntity placer, BlockPos pos) {
        Vec3d vec = placer.getPositionVec();
        Direction d = Direction.getFacingFromVector(vec.x-pos.getX(),vec.y-pos.getY(),vec.z-pos.getZ());
        if (d==Direction.DOWN || d==Direction.UP){
            return Direction.NORTH;
        }
        return d;
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
    public void onMinecartPass(BlockState state, World world, BlockPos pos, AbstractMinecartEntity cart) {
        RailShape shape = state.get(BlockStateProperties.RAIL_SHAPE_STRAIGHT);
        boolean firstWayPowered = state.get(FIRST_WAY_POW);
        Direction d = getDirectionFromState(shape,firstWayPowered);
        boolean isActivated = state.get(BlockStateProperties.POWERED);
        Vec3d vec3d = cart.getMotion();
        //done if the block is powered
        if (isActivated){
            double initialSpeed = 0.02D;
            //if the train is on the rail and is stopped we initialise its speed
            // (like powered rail when one of the two direction is blocked with
            // a opaque cube)

            if (vec3d.x == 0 && vec3d.z == 0){
                Vec3d newMotion;
                System.out.println("instantiating speed");
                switch (d){
                    case EAST:
                        newMotion = new Vec3d(-initialSpeed,0.0D,0.0D);
                        break;
                    case SOUTH:
                        newMotion = new Vec3d(0.0D,0.0D,-initialSpeed);
                        break;
                    case UP:
                    case DOWN:
                    default:
                        throw new IllegalArgumentException("no such direction authorised : "+d);
                    case WEST:
                        newMotion = new Vec3d(initialSpeed,0.0D,0.0D);
                        break;
                    case NORTH:
                        newMotion = new Vec3d(0.0D,0.0D,initialSpeed);
                }
                cart.setMotion(newMotion);
            }
            //done if the train is moving it is also done for instantiate
            // block but it will automatically be verified
            // as for other cart if it is the right direction it will accelerate it
            Direction motionDirection = Direction.getFacingFromVector(vec3d.x,vec3d.y,vec3d.z);
            if (motionDirection == d){
                //direction match so the train is accelerated
                Vec3d vec3d2 = cart.getMotion();
                double horMag = Math.sqrt(horizontalMag(vec3d2));
                double gain = 0.06D;
                cart.setMotion(vec3d2.add(new Vec3d(vec3d2.x/horMag * gain,0.0D, vec3d2.z/horMag * gain)));
                System.out.println("accelarating train");
            }
            else {
                //if it is rolling on the other direction we need to reverse its speed
                Vec3d vec3d1 = cart.getMotion();
                cart.setMotion(vec3d1.inverse());
            }
        }else {
            // if the rail is not powered it will instantly stop the rail and fix it on a position
            cart.setMotion(Vec3d.ZERO);
            cart.setVelocity(0,0,0);
            if (cart.prevPosX < pos.getX()+0.55 && cart.prevPosX> pos.getX()+0.45){
                System.out.println("fixed cart !!");
                cart.setPosition(cart.prevPosX,cart.prevPosY,cart.prevPosZ);
            }
        }
    }

}
