package fr.mattmouss.switchrail.switchblock;

import fr.mattmouss.switchrail.blocks.ControllerTile;
import fr.mattmouss.switchrail.blocks.SwitchTile;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.enum_rail.SwitchType;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class Switch extends AbstractRailBlock {

    private static AbstractMinecartEntity previous_cart = null;

    private static int i;

    public static EnumProperty<RailShape> RAIL_STRAIGHT_FLAT;
    public static EnumProperty<Corners> SWITCH_POSITION_TRIPLE;

    static {
        RAIL_STRAIGHT_FLAT = EnumProperty.create("shape",RailShape.class,(railShape -> {
            return (railShape == RailShape.NORTH_SOUTH || railShape == RailShape.EAST_WEST);
        }));
        SWITCH_POSITION_TRIPLE = EnumProperty.create("switch_position",Corners.class,(corners -> {
                return (corners != Corners.TURN);
        }));

    }



    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player){
        super.onBlockHarvested(worldIn,pos,state,player);
        if (!worldIn.getWorld().isRemote) {
            List<ControllerTile> controller_nearby = searchController(worldIn, pos);
            for (ControllerTile controller_tile : controller_nearby) {
                controller_tile.deleteSwitch((SwitchTile) worldIn.getWorld().getTileEntity(pos));
                System.out.println("safely deleted block :" + this.toString());
            }
        }
    }

    protected List<ControllerTile> searchController(IWorld world,BlockPos pos){
        List<ControllerTile> controllerTiles = new ArrayList<>();
        int i = 10;
        int j = 5;
        for (int x=pos.getX()-i; x<pos.getX()+i;x++){
            for (int y=pos.getY()-j; y<pos.getZ()+i;y++){
                for (int z=pos.getZ()-i;z<pos.getZ()+i;z++){
                    BlockPos pos_tested = new BlockPos(x,y,z);
                    TileEntity controllerTile=world.getWorld().getTileEntity(pos_tested);
                    if (controllerTile instanceof ControllerTile){
                        controllerTiles.add((ControllerTile) controllerTile);
                    }
                }
            }
        }

        return controllerTiles;
    };

    @Override
    public IProperty<RailShape> getShapeProperty() {
        return RAIL_STRAIGHT_FLAT;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SwitchTile();
    }
    
    protected Switch(boolean p_i48444_1_, Properties p_i48444_2_) {
        super(true, p_i48444_2_);
    }
    
    public BlockState getBlockState(World world, BlockPos pos){
        return world.getBlockState(pos);
    }

    public abstract void updatePoweredState(World world, BlockState state, BlockPos pos, PlayerEntity player, int flag,boolean fromScreen);

    protected Direction getFacingFromEntity(LivingEntity entity, BlockPos pos) {
        Vec3d vec3d = entity.getPositionVec();
        Direction d= Direction.getFacingFromVector(
                (float) (vec3d.x-pos.getX()),
                (float) (vec3d.y-pos.getY()),
                (float) (vec3d.z-pos.getZ()));
        if (d==Direction.UP || d==Direction.DOWN){
            return Direction.NORTH;
        }else {
            return d.getOpposite();
        }
    }

    protected RailShape updateRailShapeFromEntity(LivingEntity entity, BlockPos pos) {
        Vec3d vec3d = entity.getPositionVec();
        Direction d= Direction.getFacingFromVector(
                (float) (vec3d.x-pos.getX()),
                (float) (vec3d.y-pos.getY()),
                (float) (vec3d.z-pos.getZ()));
        if (d==Direction.EAST || d==Direction.WEST)
            return RailShape.EAST_WEST;
        else if (d==Direction.NORTH || d==Direction.SOUTH)
            return RailShape.NORTH_SOUTH;
        else return RailShape.EAST_WEST;
    }





    @Override
    public boolean isIn(Tag<Block> tag) {
        return (tag == BlockTags.RAILS);
    }

    public abstract SwitchType getType();




    @Override
    protected BlockState getUpdatedState(World p_208489_1_, BlockPos p_208489_2_, BlockState p_208489_3_, boolean placing) {
        System.out.println("getUpdatedState appele");
        return p_208489_3_;
    }

    /*
    @Override
    public void onMinecartPass(BlockState state, World world, BlockPos pos, AbstractMinecartEntity cart) {
        System.out.println("Position du minecart : ( "+cart.lastTickPosX+" ; "+cart.lastTickPosY+" ; "+cart.lastTickPosZ+" )");
        System.out.println("Position du switch :"+pos);
        System.out.println("entier increment :"+i);
        if (previous_cart == null){
            previous_cart = cart;
            i=0;
        }
        if (previous_cart.equals(cart)){
            if (i !=10) i++;

        }else {
            i =0;
        }
        if (i != 10) Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.BLOCK_METAL_BREAK,1.0F));

    }
     */

}
