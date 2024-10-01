package fr.moonshade.switchrail.other;

import com.dannyandson.tinyredstone.api.IPanelCell;
import com.dannyandson.tinyredstone.blocks.PanelCellPos;
import com.dannyandson.tinyredstone.blocks.PanelTile;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import fr.moonshade.switchrail.axle_point.WorldCounterPoints;
import fr.moonshade.switchrail.blocks.AxleCounterTile;
import fr.moonshade.switchrail.blocks.IPosZoomStorageHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import com.mojang.math.Matrix4f;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Util {

    public static final Vector2i DEFAULT_ZOOM = new Vector2i(16,11);

    public static Vec2 add(Vec2... vectors){
        float x = 0.0F;
        float y = 0.0F;
        for (Vec2 vec : vectors){
            x += vec.x;
            y += vec.y;
        }
        return new Vec2(x,y);
    }

    public static Vec2 subtract(Vec2 vec1, Vec2 vec2){
        return new Vec2(vec1.x-vec2.x,vec1.y-vec2.y);
    }

    public static Vec2 scale(Vec2 vec,float scale){
        return new Vec2(scale*vec.x,scale* vec.y);
    }

    public static Vec2 makeVector(BlockPos pos){
        return new Vec2(pos.getX(),pos.getZ());
    }

    public static Vec2 directMult(Vec2 vec1,Vec2 vec2){
        return new Vec2(vec1.x*vec2.x,vec1.y* vec2.y);
    }

    public static Vec2 makeVector(float scale){
        return scale(Vec2.ONE,scale);
    }

    public static boolean isBiggerThan(Vec2 vec1,Vec2 vec2){
        return vec1.x>= vec2.x && vec1.y>=vec2.y;
    }

    public static boolean isIn(Vec2 start,Vec2 dimension,double mouseX, double mouseY){
        Vec2 mousePosition = new Vec2((float) mouseX,(float) mouseY);
        return isBiggerThan(mousePosition,start) && isBiggerThan(add(start,dimension),mousePosition);
    }

    public static Direction getDirectionFromEntity(LivingEntity placer, BlockPos pos){
        return getDirectionFromEntity(placer,pos,true);
    }

    public static Direction getDirectionFromEntity(LivingEntity placer, BlockPos pos,boolean takeOpposite){
        Vec3 vec3d = placer.position();
        Direction d= Direction.getNearest(vec3d.x-pos.getX(),vec3d.y-pos.getY(),vec3d.z-pos.getZ());
        if (d== Direction.DOWN || d== Direction.UP){
            return Direction.NORTH;
        }
        return takeOpposite?d.getOpposite():d;
    }

    public static DoorHingeSide getHingeSideFromEntity(LivingEntity entity, BlockPos pos, Direction direction) {
        return switch (direction) {
            default -> throw new IllegalArgumentException("No such direction authorised !!");
            case NORTH -> (entity.position().x < pos.getX() + 0.5) ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            case SOUTH -> (entity.position().x < pos.getX() + 0.5) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            case WEST -> (entity.position().z < pos.getZ() + 0.5) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            case EAST -> (entity.position().z < pos.getZ() + 0.5) ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
        };
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
        Vec3 vec =placer.position();
        Direction dir = Direction.getNearest(vec.x-pos.getX(),vec.y-pos.getY(),vec.z-pos.getZ());
        if (dir == Direction.UP || dir == Direction.DOWN){
            dir = Direction.NORTH;
        }
        if (needOpposite)return dir.getOpposite();
        return dir;
    }

    public static void putPos(CompoundTag nbt, BlockPos pos){
        nbt.putLong("position",pos.asLong());
    }

    public static BlockPos getPosFromNbt(CompoundTag nbt){
        long value = nbt.getLong("position");
        return BlockPos.of(value);
    }

    //gives the axis direction that is the motion of the train from the rail-shape
    public static Direction.Axis getRailShapeAxis(RailShape shape){
        return switch (shape) {
            case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> Direction.Axis.X;
            case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> Direction.Axis.Z;
            default -> null;
        };
    }

    public static void renderQuad(PoseStack stack, Vec2 origin, Vec2 end, Vec2 uvOrigin, Vec2 uvEnd) {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix4f = stack.last().pose();
        bufferbuilder.vertex(matrix4f, origin.x, origin.y, (float) 0).uv(uvOrigin.x, uvOrigin.y).endVertex();
        bufferbuilder.vertex(matrix4f, origin.x, end.y, (float) 0).uv(uvOrigin.x, uvEnd.y).endVertex();
        bufferbuilder.vertex(matrix4f, end.x, end.y, (float) 0).uv(uvEnd.x, uvEnd.y).endVertex();
        bufferbuilder.vertex(matrix4f, end.x, origin.y, (float) 0).uv(uvEnd.x, uvOrigin.y).endVertex();
        tessellator.end();
    }

    public static AxleCounterTile getAxleTileEntity(Level world, BlockPos pos){
        BlockEntity tile = world.getBlockEntity(pos);
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
    public static <T> void writeMap(CompoundTag nbt, String tag, HashMap<BlockPos,T> map, BiConsumer<CompoundTag,T> writeTinNBTConsumer){
        if (!map.isEmpty()){
            ListTag listNBT = new ListTag();
            map.forEach(((pos, t) -> {
                CompoundTag internNBT = new CompoundTag();
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
    public static <T> HashMap<BlockPos,T> readMap(CompoundTag nbt, String tag, Function<CompoundTag,T> readTinNBTFunction){
        HashMap<BlockPos,T> map = new HashMap<>();
        if (nbt.contains(tag)){
            Tag inbt = nbt.get(tag);
            if (!(inbt instanceof ListTag)){
                throw new IllegalStateException("Error in loading of nbt : the NBT stored is not a list ! Use this function with writeMap !");
            }
            ListTag listNBT = (ListTag) (nbt.get(tag));
            assert listNBT != null;
            listNBT.forEach(inbt1 -> {
                if (!(inbt1 instanceof CompoundTag)){
                    throw new IllegalStateException("Error in loading of nbt : the NBT stored is not a compound NBT ! Use this function with writeMap !");
                }
                CompoundTag internNBT = (CompoundTag) inbt1;
                T t = readTinNBTFunction.apply(internNBT);
                BlockPos pos = Util.getPosFromNbt(internNBT);
                map.put(pos,t);
            });
        }
        return map;
    }

    public static WorldCounterPoints getWorldCounterPoint(Level world){
        return Objects.requireNonNull(world.getServer()).overworld().getDataStorage().computeIfAbsent(WorldCounterPoints::new,WorldCounterPoints::new,"world_cp");
        //return Objects.requireNonNull(world.getServer()).overworld()
        //     .getDataStorage().computeIfAbsent(WorldCounterPoints::new,"world_cp");
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
        BlockEntity te = Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(tePos);
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
