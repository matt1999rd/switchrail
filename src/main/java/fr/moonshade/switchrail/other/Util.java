package fr.moonshade.switchrail.other;

import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import fr.moonshade.switchrail.axle_point.WorldCounterPoints;
import fr.moonshade.switchrail.blocks.AxleCounterTile;
import fr.moonshade.switchrail.blocks.IPosZoomStorageHandler;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Util {

    public static final Vector2i DEFAULT_ZOOM = new Vector2i(16,11);

    public static Vector2f add(Vector2f... vectors){
        float x = 0.0F;
        float y = 0.0F;
        for (Vector2f vec : vectors){
            x += vec.x;
            y += vec.y;
        }
        return new Vector2f(x,y);
    }

    public static Vector2f subtract(Vector2f vec1, Vector2f vec2){
        return new Vector2f(vec1.x-vec2.x,vec1.y-vec2.y);
    }

    public static Vector2f scale(Vector2f vec,float scale){
        return new Vector2f(scale*vec.x,scale* vec.y);
    }

    public static Vector2f makeVector(BlockPos pos){
        return new Vector2f(pos.getX(),pos.getZ());
    }

    public static Vector2f directMult(Vector2f vec1,Vector2f vec2){
        return new Vector2f(vec1.x*vec2.x,vec1.y* vec2.y);
    }

    public static Vector2f makeVector(float scale){
        return scale(Vector2f.ONE,scale);
    }

    public static boolean isBiggerThan(Vector2f vec1,Vector2f vec2){
        return vec1.x>= vec2.x && vec1.y>=vec2.y;
    }

    public static boolean isIn(Vector2f start,Vector2f dimension,double mouseX, double mouseY){
        Vector2f mousePosition = new Vector2f((float) mouseX,(float) mouseY);
        return isBiggerThan(mousePosition,start) && isBiggerThan(add(start,dimension),mousePosition);
    }

    public static Direction getDirectionFromEntity(LivingEntity placer, BlockPos pos){
        return getDirectionFromEntity(placer,pos,true);
    }

    public static Direction getDirectionFromEntity(LivingEntity placer, BlockPos pos,boolean takeOpposite){
        Vector3d vec3d = placer.position();
        Direction d= Direction.getNearest(vec3d.x-pos.getX(),vec3d.y-pos.getY(),vec3d.z-pos.getZ());
        if (d== Direction.DOWN || d== Direction.UP){
            return Direction.NORTH;
        }
        return takeOpposite?d.getOpposite():d;
    }

    public static DoorHingeSide getHingeSideFromEntity(LivingEntity entity, BlockPos pos, Direction direction) {
        switch (direction){
            case DOWN:
            case UP:
            default:
                throw new IllegalArgumentException("No such direction authorised !!");
            case NORTH:
                return (entity.position().x<pos.getX()+0.5)?DoorHingeSide.RIGHT:DoorHingeSide.LEFT;
            case SOUTH:
                return (entity.position().x<pos.getX()+0.5)?DoorHingeSide.LEFT:DoorHingeSide.RIGHT;
            case WEST:
                return (entity.position().z<pos.getZ()+0.5)?DoorHingeSide.LEFT:DoorHingeSide.RIGHT;
            case EAST:
                return (entity.position().z<pos.getZ()+0.5)?DoorHingeSide.RIGHT:DoorHingeSide.LEFT;
        }
    }

    public static RailShape getShapeFromDirection(Direction direction1,Direction direction2){
        if (direction1 == direction2 || direction1.getAxis() == Direction.Axis.Y || direction2.getAxis() == Direction.Axis.Y)return null;
        if (direction1.getAxis() == direction2.getAxis()){
            return (direction1.getAxis() == Direction.Axis.X)? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
        }
        if (direction2 == Direction.NORTH || direction2 == Direction.SOUTH)return getShapeFromDirection(direction2,direction1);
        return RailShape.valueOf(direction1.getName().toUpperCase()+"_"+direction2.getName().toUpperCase());
    }

    public static Direction getFacingFromEntity(LivingEntity placer, BlockPos pos,boolean needOpposite) {
        Vector3d vec =placer.position();
        Direction dir = Direction.getNearest(vec.x-pos.getX(),vec.y-pos.getY(),vec.z-pos.getZ());
        if (dir == Direction.UP || dir == Direction.DOWN){
            dir = Direction.NORTH;
        }
        if (needOpposite)return dir.getOpposite();
        return dir;
    }

    public static void putPos(CompoundNBT nbt, BlockPos pos){
        nbt.putLong("position",pos.asLong());
    }

    public static BlockPos getPosFromNbt(CompoundNBT nbt){
        long value = nbt.getLong("position");
        return BlockPos.of(value);
    }

    //gives the axis direction that is the motion of the train from the rail-shape
    public static Direction.Axis getRailShapeAxis(RailShape shape){
        switch (shape){
            case EAST_WEST:
            case ASCENDING_EAST:
            case ASCENDING_WEST:
                return Direction.Axis.X;
            case NORTH_SOUTH:
            case ASCENDING_NORTH:
            case ASCENDING_SOUTH:
                return Direction.Axis.Z;
            default:
                return null;
        }
    }

    public static void renderQuad(MatrixStack stack, Vector2f origin, Vector2f end, Vector2f uvOrigin, Vector2f uvEnd, boolean isEnable){
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        Matrix4f matrix4f = stack.last().pose();
        float colorMask = (isEnable) ? 1.0F : 0.5F;
        RenderSystem.color3f(colorMask,colorMask,colorMask);
        bufferbuilder.vertex(matrix4f, origin.x, origin.y, (float)0).uv(uvOrigin.x, uvOrigin.y).endVertex();
        bufferbuilder.vertex(matrix4f, origin.x, end.y, (float)0).uv(uvOrigin.x, uvEnd.y).endVertex();
        bufferbuilder.vertex(matrix4f, end.x, end.y, (float)0).uv(uvEnd.x, uvEnd.y).endVertex();
        bufferbuilder.vertex(matrix4f, end.x, origin.y, (float)0).uv(uvEnd.x, uvOrigin.y).endVertex();
        tessellator.end();
        RenderSystem.color3f(1.0F,1.0F,1.0F);
    }

    public static AxleCounterTile getAxleTileEntity(World world, BlockPos pos){
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof AxleCounterTile) {
            return (AxleCounterTile) tile;
        }else {
            throw new IllegalStateException("Error on Minecart pass behaviour : Axle counter in question has no axle counter tile ! The tile in question is : "+tile);
        }
    }

    // write map function is adding to the compoundNBT nbt the HashMap map with the string tag as UUID,
    // and it uses generify type T to represent (on switchrail only !) Set<CounterPoint> or UUID.
    // writeTinNBTConsumer is a function that indicates how to save variable of type T in the given CompoundNBT
    // this function takes in account if the map is empty
    // Use it with the readMap function
    public static <T> void writeMap(CompoundNBT nbt, String tag, HashMap<BlockPos,T> map, BiConsumer<CompoundNBT,T> writeTinNBTConsumer){
        if (!map.isEmpty()){
            ListNBT listNBT = new ListNBT();
            map.forEach(((pos, t) -> {
                CompoundNBT internNBT = new CompoundNBT();
                writeTinNBTConsumer.accept(internNBT,t);
                Util.putPos(internNBT,pos);
                listNBT.add(internNBT);
            }));
            nbt.put(tag,listNBT);
        }
    }

    // read map function is extracting from the compoundNBT nbt the HashMap stored with the string tag as UUID,
    // and it uses generify type T to represent (on switchrail only !) Set<CounterPoint> or UUID.
    // readTinNBTFunction is a function that indicates how to extract variable of type T in the given CompoundNBT
    // this function takes in account if the map is empty and return an empty map if it is the case
    // Use it with the writeMap function
    public static <T> HashMap<BlockPos,T> readMap(CompoundNBT nbt, String tag, Function<CompoundNBT,T> readTinNBTFunction){
        HashMap<BlockPos,T> map = new HashMap<>();
        if (nbt.contains(tag)){
            INBT inbt = nbt.get(tag);
            if (!(inbt instanceof ListNBT)){
                throw new IllegalStateException("Error in loading of nbt : the NBT stored is not a list ! Use this function with writeMap !");
            }
            ListNBT listNBT = (ListNBT) (nbt.get(tag));
            assert listNBT != null;
            listNBT.forEach(inbt1 -> {
                if (!(inbt1 instanceof CompoundNBT)){
                    throw new IllegalStateException("Error in loading of nbt : the NBT stored is not a compound NBT ! Use this function with writeMap !");
                }
                CompoundNBT internNBT = (CompoundNBT) inbt1;
                T t = readTinNBTFunction.apply(internNBT);
                BlockPos pos = Util.getPosFromNbt(internNBT);
                map.put(pos,t);
            });
        }
        return map;
    }

    public static WorldCounterPoints getWorldCounterPoint(World world){
        return Objects.requireNonNull(world.getServer()).overworld()
                .getDataStorage().computeIfAbsent(WorldCounterPoints::new,"world_cp");
    }

    //this function is a string test to know if the shape "shape" has a part in the direction "direction"
    public static boolean test(RailShape shape,Direction direction){
        return shape == RailShape.valueOf("ASCENDING_"+direction.getOpposite().getName().toUpperCase()) || shape.getSerializedName().contains(direction.getName());
    }

    //this function decompose the Rail shape as a pair of two directions (the horizontal direction where the rail is leading the train)
    public static Pair<Direction,Direction> getDirections(RailShape shape){
        if (shape == null)return null;
        if (shape.isAscending()){
            int n = "ascending_".length();
            String name = shape.getSerializedName().substring(n);
            Direction dir = Direction.byName(name);
            assert dir != null;
            return Pair.of(dir,dir.getOpposite()); //specificity : if ascending west or ascending south is given, we get west east and south north
        }
        String[] str = shape.getSerializedName().split("_");
        // str has length 2
        return Pair.of(Direction.byName(str[0]),Direction.byName(str[1]));
    }

    //this function extract the IPosZoomStorageHandler from the context object of a packet send through network
    public static IPosZoomStorageHandler extractHandler(Supplier<NetworkEvent.Context> context, BlockPos tePos, int index){
        TileEntity te = Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(tePos);
        if (index != -1){
            if (te instanceof PanelTile){
                PanelTile panelTile = (PanelTile)te;
                IPanelCell panelCell = panelTile.getIPanelCell(PanelCellPos.fromIndex(panelTile,index));
                if (panelCell instanceof IPosZoomStorageHandler){
                    return (IPosZoomStorageHandler) panelCell;
                }
            }
        }else {
            if (te instanceof IPosZoomStorageHandler){
                return (IPosZoomStorageHandler) te;
            }
        }
        throw new IllegalStateException("Expect either a tile entity or a cell object (within the panel tile entity) with pos and zoom storage !");
    }

}
