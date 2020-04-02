package fr.mattmouss.switchrail.switchblock;


import fr.mattmouss.switchrail.SwitchRailMod;
import fr.mattmouss.switchrail.enum_rail.Corners;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.RailShape;

import net.minecraft.util.math.BlockPos;

import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.logging.Logger;




public abstract class Switch_Straight extends Switch {

    private static final Logger logger = Logger.getLogger(SwitchRailMod.MODID);

    public static EnumProperty<Corners> SWITCH_POSITION ;

    static  {
        SWITCH_POSITION = EnumProperty.create("switch_position",Corners.class,(corner)-> (corner == Corners.STRAIGHT || corner == Corners.TURN));
    }




    public Switch_Straight(){
        super(true,Properties.create(Material.IRON)
                .lightValue(0)
                .hardnessAndResistance(2f)
                .doesNotBlockMovement()
                .sound(SoundType.METAL));
        this.setDefaultState(this.stateContainer.getBaseState().with(RAIL_STRAIGHT_FLAT,RailShape.NORTH_SOUTH).with(SWITCH_POSITION,Corners.STRAIGHT));
    }

    public Switch_Straight(Properties builder) {
        super(true,builder);


    }
    @Override
    public void onBlockPlacedBy(World world , BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity !=null) {
            world.setBlockState(pos,state
                    .with(RAIL_STRAIGHT_FLAT,updateRailShapeFromEntity(entity, pos))
                    .with(SWITCH_POSITION,Corners.STRAIGHT)
            );
        }
    }




    @Override
    public void fillStateContainer(StateContainer.Builder<Block,BlockState> builder){
        builder.add(RAIL_STRAIGHT_FLAT,
                SWITCH_POSITION);
    }

    public void updatePoweredState(World world, BlockState state, BlockPos pos, PlayerEntity player, int flags,boolean fromScreen) {
        if (!world.isRemote || fromScreen){
            Corners actualState = state.get(SWITCH_POSITION);

            boolean isPowered = (actualState == Corners.TURN);
            RailShape currentRailShape = world.getBlockState(pos).get(RAIL_STRAIGHT_FLAT);
            if (!isPowered){
                System.out.println("changing to turn position");
                world.setBlockState(pos,
                        state.with(SWITCH_POSITION,Corners.TURN)
                                .with(RAIL_STRAIGHT_FLAT,currentRailShape)
                        ,flags);

            }else {
                System.out.println("changing to straight position");
                world.setBlockState(pos,
                        state.with(SWITCH_POSITION,Corners.STRAIGHT)
                                .with(RAIL_STRAIGHT_FLAT,currentRailShape)
                        ,flags);
            }
        }
    }



    @Override
    public boolean isFlexibleRail(BlockState p_isFlexibleRail_1_, IBlockReader p_isFlexibleRail_2_, BlockPos p_isFlexibleRail_3_) {
        return false;
    }

    @Override
    public IProperty<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }

    @Override
    public boolean canMakeSlopes(BlockState p_canMakeSlopes_1_, IBlockReader p_canMakeSlopes_2_, BlockPos p_canMakeSlopes_3_) {
        return false;
    }


    @Override
    protected BlockState getUpdatedState(World world, BlockPos pos, BlockState state, boolean b) {
        return state;
    }


}