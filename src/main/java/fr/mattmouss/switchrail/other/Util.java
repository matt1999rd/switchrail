package fr.mattmouss.switchrail.other;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;

import java.util.Locale;

public class Util {
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
        Vector3d vec3d = placer.position();
        Direction d= Direction.getNearest(vec3d.x-pos.getX(),vec3d.y-pos.getY(),vec3d.z-pos.getZ());
        if (d== Direction.DOWN || d== Direction.UP){
            return Direction.NORTH;
        }
        return d.getOpposite();
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

    public static CompoundNBT putPos(CompoundNBT nbt,BlockPos pos){
        nbt.putLong("position",pos.asLong());
        return nbt;
    }

    public static BlockPos getPosFromNbt(CompoundNBT nbt){
        long value = nbt.getLong("position");
        return BlockPos.of(value);
    }



}
