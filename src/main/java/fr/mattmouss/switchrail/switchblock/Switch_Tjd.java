package fr.mattmouss.switchrail.switchblock;

import fr.mattmouss.switchrail.enum_rail.Tjd_Position;
import fr.mattmouss.switchrail.enum_rail.SwitchType;
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
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

//verifier le bon fonctionnement de getRailShape()
//tout le reste fonctionne

public class Switch_Tjd extends Switch {

    public static EnumProperty<Tjd_Position> SWITCH_POSITION;

    public static final DirectionProperty FACING_AXE = DirectionProperty.create("facing_axe", Direction.NORTH,Direction.EAST);

    static  {
        SWITCH_POSITION=EnumProperty.create("switch_position",Tjd_Position.class);
    }

    private RailShape fixedRailShape= RailShape.NORTH_SOUTH;

    protected Switch_Tjd(boolean p_i48444_1_, Properties p_i48444_2_) {
        super(true, p_i48444_2_);
        this.setRegistryName("switch_tjd");
    }



    //RailShape.NORTH_SOUTH model classique
    //RailShape.EAST_WEST model avec West à la place de North

    public Switch_Tjd() {
        super(true,Properties.create(Material.IRON)
                .doesNotBlockMovement()
                .lightValue(0)
                .hardnessAndResistance(2f)
                .sound(SoundType.METAL));
        this.setRegistryName("switch_tjd");
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(SWITCH_POSITION, Tjd_Position.NO_POWER)
                .with(FACING_AXE,Direction.NORTH)
                .with(RAIL_STRAIGHT_FLAT, RailShape.NORTH_SOUTH)
        );
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack p_180633_5_) {
        if (entity !=null) {

            world.setBlockState(pos,state
                    .with(SWITCH_POSITION,Tjd_Position.NO_POWER)
                    .with(FACING_AXE,getFacingFromEntity(entity, pos))
                    .with(RAIL_STRAIGHT_FLAT, RailShape.NORTH_SOUTH)
            );
        }
    }

    @Override
    protected Direction getFacingFromEntity(LivingEntity entity,BlockPos pos) {
        Direction d = super.getFacingFromEntity(entity, pos);
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
    public SwitchType getType() {
        return SwitchType.TJD;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(SWITCH_POSITION,FACING_AXE,RAIL_STRAIGHT_FLAT);
    }
    //blockPos transforme les double en int
    @Override
    public void updatePoweredState(World world, BlockState state, BlockPos pos, PlayerEntity player, int flags,boolean fromScreen) {
        double[] pos_ru_sw;
        double[] pos_ld_sw;
        pos_ru_sw=(state.get(FACING_AXE) == Direction.NORTH)?
                new double[]{pos.getX() + 0.9, pos.getY() + 0.1, pos.getZ() + 0.1}:
                new double[]{pos.getX() + 0.9, pos.getY() + 0.1, pos.getZ() + 0.9};
        pos_ld_sw = (state.get(FACING_AXE) == Direction.NORTH)?
                new double[]{pos.getX() + 0.1, pos.getY() + 0.1, pos.getZ() + 0.9}:
                new double[]{pos.getX() + 0.1, pos.getY() + 0.1, pos.getZ() + 0.1};
        //determine la position théorique des petit cube de l'aiguille de manière à changer la plus proche
        if (!world.isRemote || fromScreen) {
            Vec3d vec3d = player.getPositionVector();
            double[] player_pos = {vec3d.x, vec3d.y, vec3d.z};
            System.out.println("distance avec le switch right up :" + Distance3(pos_ru_sw, player_pos));
            System.out.println("distance avec le switch left down :" + Distance3(pos_ld_sw, player_pos));
            System.out.println("switch le plus proche :" +
                    ((Distance3(pos_ru_sw, player_pos) > Distance3(pos_ld_sw, player_pos)) ?
                            "switch left down" :
                            "switch up right"));
            Direction currentDirection = world.getBlockState(pos).get(FACING_AXE);

            Tjd_Position actualState = world.getBlockState(pos).get(SWITCH_POSITION);

            updatePowerState(world,state,pos,flags,
                    Distance3(pos_ru_sw, player_pos) > Distance3(pos_ld_sw, player_pos)
                    ,actualState,currentDirection);
            /*

            if (Distance3(pos_ru_sw, player_pos) > Distance3(pos_ld_sw, player_pos)) {
                //le switch left down est plus proche du joueur que le switch right up
                //left down change

                switch (actualState) {
                    case NO_POWER:
                        System.out.println("changing sld to turn position ");
                        world.setBlockState(pos,
                                state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_LEFT_DOWN)
                                        .with(FACING_AXE, currentDirection)

                                , flags);
                        break;
                    case POWER_ON_RIGHT_UP:
                        System.out.println("changing sld to turn position ");
                        world.setBlockState(pos,
                                state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_BOTH)
                                        .with(FACING_AXE, currentDirection)
                                , flags);
                        break;
                    case POWER_ON_LEFT_DOWN:
                        System.out.println("changing sld to straight position ");
                        world.setBlockState(pos,
                                state.with(SWITCH_POSITION, Tjd_Position.NO_POWER)
                                        .with(FACING_AXE, currentDirection)
                                , flags);
                        break;
                    case POWER_ON_BOTH:
                        System.out.println("changing sld to straight position ");
                        world.setBlockState(pos,
                                state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_RIGHT_UP)
                                        .with(FACING_AXE, currentDirection)
                                , flags);
                        break;
                }
            } else {
                //le switch right up est plus proche du joueur que le switch left down
                //right up change
                switch (actualState) {
                    case NO_POWER:
                        System.out.println("changing sru to turn position ");
                        world.setBlockState(pos,
                                state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_RIGHT_UP)
                                        .with(FACING_AXE, currentDirection)
                                , flags);
                        break;
                    case POWER_ON_RIGHT_UP:
                        System.out.println("changing sru to straight position ");
                        world.setBlockState(pos,
                                state.with(SWITCH_POSITION, Tjd_Position.NO_POWER)
                                        .with(FACING_AXE, currentDirection)
                                , flags);
                        break;
                    case POWER_ON_LEFT_DOWN:
                        System.out.println("changing sru to turn position ");
                        world.setBlockState(pos,
                                state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_BOTH)
                                        .with(FACING_AXE, currentDirection)
                                , flags);
                        break;
                    case POWER_ON_BOTH:
                        System.out.println("changing sld to straight position ");
                        world.setBlockState(pos,
                                state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_LEFT_DOWN)
                                        .with(FACING_AXE, currentDirection)
                                , flags);
                        break;
                }



            }
*/

        }
    }

    public void updatePowerState(World world,BlockState state,BlockPos pos,int flags,boolean ld_nearest,Tjd_Position actualState,Direction currentDirection){
        if (ld_nearest){
            //le switch left down est plus proche du joueur que le switch right up
            //left down change

            switch (actualState) {
                case NO_POWER:
                    System.out.println("changing sld to turn position ");
                    world.setBlockState(pos,
                            state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_LEFT_DOWN)
                                    .with(FACING_AXE, currentDirection)

                            , flags);
                    break;
                case POWER_ON_RIGHT_UP:
                    System.out.println("changing sld to turn position ");
                    world.setBlockState(pos,
                            state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_BOTH)
                                    .with(FACING_AXE, currentDirection)
                            , flags);
                    break;
                case POWER_ON_LEFT_DOWN:
                    System.out.println("changing sld to straight position ");
                    world.setBlockState(pos,
                            state.with(SWITCH_POSITION, Tjd_Position.NO_POWER)
                                    .with(FACING_AXE, currentDirection)
                            , flags);
                    break;
                case POWER_ON_BOTH:
                    System.out.println("changing sld to straight position ");
                    world.setBlockState(pos,
                            state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_RIGHT_UP)
                                    .with(FACING_AXE, currentDirection)
                            , flags);
                    break;
            }
        }else {
            //le switch right up est plus proche du joueur que le switch left down
            //right up change
            switch (actualState) {
                case NO_POWER:
                    System.out.println("changing sru to turn position ");
                    world.setBlockState(pos,
                            state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_RIGHT_UP)
                                    .with(FACING_AXE, currentDirection)
                            , flags);
                    break;
                case POWER_ON_RIGHT_UP:
                    System.out.println("changing sru to straight position ");
                    world.setBlockState(pos,
                            state.with(SWITCH_POSITION, Tjd_Position.NO_POWER)
                                    .with(FACING_AXE, currentDirection)
                            , flags);
                    break;
                case POWER_ON_LEFT_DOWN:
                    System.out.println("changing sru to turn position ");
                    world.setBlockState(pos,
                            state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_BOTH)
                                    .with(FACING_AXE, currentDirection)
                            , flags);
                    break;
                case POWER_ON_BOTH:
                    System.out.println("changing sld to straight position ");
                    world.setBlockState(pos,
                            state.with(SWITCH_POSITION, Tjd_Position.POWER_ON_LEFT_DOWN)
                                    .with(FACING_AXE, currentDirection)
                            , flags);
                    break;
                }
            }
        }


    private double Distance3(double[] first_pos, double[] player_pos) {

        return MathHelper.sqrt(
                Math.pow(first_pos[0]-player_pos[0],2.0)+
               Math.pow(first_pos[1]-player_pos[1],2.0)+
               Math.pow(first_pos[2]-player_pos[2],2.0)) ;

    }

    private double Distance2(double[] first_pos, double[] player_pos) {

        return MathHelper.sqrt(
                Math.pow(first_pos[0]-player_pos[0],2.0)+
                        Math.pow(first_pos[1]-player_pos[1],2.0)) ;

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


    public boolean minecartArrivedOnBlock(AbstractMinecartEntity entity, BlockPos pos) {
        return (entity.getEntityWorld().isRemote ||
                entity.prevPosX > pos.getX() &&
                        entity.prevPosX < pos.getX()+1 &&
                        entity.prevPosZ > pos.getZ() &&
                        entity.prevPosZ < pos.getZ()+1
        );
    }


    public RailShape getRailShapeFromEntityAndState(BlockPos pos, AbstractMinecartEntity entity, BlockState state) {
        Tjd_Position tjd_position = entity.getEntityWorld().getBlockState(pos).get(SWITCH_POSITION);
        Direction tjd_dir = entity.getEntityWorld().getBlockState(pos).get(FACING_AXE);

        //le minecart vient de l'ouest
        if (entity.prevPosX<pos.getX()){
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
        //le minecart vient de l'est
        }else if (entity.prevPosX> pos.getX()+1) {
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
        //le minecart vient du nord
        }else if (entity.prevPosZ<pos.getZ()) {
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
        //le minecart vient du sud
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
