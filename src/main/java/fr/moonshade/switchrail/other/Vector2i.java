package fr.moonshade.switchrail.other;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3i;

public class Vector2i {
    public int x,y;
    public Vector2i(int x,int y){
        this.x = x;
        this.y = y;
    }

    public Vector2i(Vector2i vec2i) {
        this.x = vec2i.x;
        this.y = vec2i.y;
    }

    public Vector2f toFloatVector(){
        return new Vector2f((float) x,(float) y);
    }

    public static Vector2i project2D(Vector3i vec, Direction.Axis removedAxis){
        switch (removedAxis) {
            case X:
                return new Vector2i(vec.getY(), vec.getZ());
            case Y:
                return new Vector2i(vec.getX(), vec.getZ());
            case Z:
                return new Vector2i(vec.getX(), vec.getY());
            default:
                throw new IllegalStateException("Impossible situation as axis is always X, Y or Z. Thanks switch for this useless line of program !");
        }
    }

}
