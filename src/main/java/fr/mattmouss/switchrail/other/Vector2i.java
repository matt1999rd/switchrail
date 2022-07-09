package fr.mattmouss.switchrail.other;

import net.minecraft.util.math.vector.Vector2f;

public class Vector2i {
    public int x,y;
    public Vector2i(int x,int y){
        this.x = x;
        this.y = y;
    }

    public Vector2f toFloatVector(){
        return new Vector2f((float) x,(float) y);
    }

}
