package fr.mattmouss.switchrail.setup;

import net.minecraft.world.level.block.Block;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

//tools for voxel-shapes
public class VoxelInts {

    private final int[] plane_val ;
    public VoxelInts(int x1, int y1, int z1, int x2, int y2, int z2){
        if (x1>16 || x2>16 || y1>16 || y2>16 || z1>16 ||z2>16){
            throw new ExceptionInInitializerError("none of this 6 int are greater than 15");
        }
        plane_val= new int[]{x1, y1, z1, x2, y2, z2};
    }

    //rotate around given axis with angle of 90° * number_of_rotation counter clock wise
    public VoxelInts rotateCCW(int number_of_rotation, Direction.Axis axis){
        number_of_rotation = number_of_rotation%4;
        return this.rotateCW(4-number_of_rotation,axis);
    }

    //rotate around given axis with angle of 90° * number_of_rotation clock wise
    public VoxelInts rotateCW(int number_of_rotation, Direction.Axis axis){
        number_of_rotation = number_of_rotation%4;
        int x1 = plane_val[0];
        int y1 = plane_val[1];
        int z1 = plane_val[2];
        int x2 = plane_val[3];
        int y2 = plane_val[4];
        int z2 = plane_val[5];
        switch (number_of_rotation){
            case 0:
                return this;
            case 1:
                switch (axis){
                    case X:
                        return new VoxelInts(x1,16-z1,y1,x2,16-z2,y2);
                    case Y:
                        return new VoxelInts(16-z1,y1,x1,16-z2,y2,x2);
                    case Z:
                        return new VoxelInts(y1,16-x1,z1,y2,16-x2,z2);
                }
            case 2:
                return this.rotateCW(1,axis).rotateCW(1,axis);
            case 3:
                return this.rotateCW(2,axis).rotateCW(1,axis);
            default:
                throw new IllegalStateException("Unexpected value: " + number_of_rotation);
        }

    }

    public VoxelInts rotate(Direction present_direction, Direction changing_direction){
        if (present_direction == changing_direction){
            return this;
        }
        //see docs in VoxelDoubles class in Gates Mod
        if (present_direction.getAxis() == changing_direction.getAxis()){
            Direction.Axis other_axis = (present_direction.getAxis().isHorizontal() ? Direction.Axis.Y : Direction.Axis.X);
            return this.rotateCW(2,other_axis);
        }

        for (Direction.Axis axis : Direction.Axis.values()){
            if (isRotatedCW(axis,present_direction,changing_direction)){
                return this.rotateCW(1, axis);
            }

            if (isRotatedCCW(axis,present_direction,changing_direction)){
                return this.rotateCCW(1,axis);
            }

        }
        return null;

    }

    private boolean isRotatedCCW(Direction.Axis axis, Direction present_direction, Direction changing_direction) {
        return isRotatedCW(axis,changing_direction,present_direction);
    }


    private boolean isRotatedCW(Direction.Axis axis, Direction present_direction, Direction changing_direction) {
        int pd_ind = present_direction.get3DDataValue();
        int cd_ind = changing_direction.get3DDataValue();
        switch (axis){
            case X:
                return (pd_ind==0 && cd_ind==2) ||
                        (pd_ind==1 && cd_ind==3) ||
                        (pd_ind==2 && cd_ind==1) ||
                        (pd_ind==3 && cd_ind==0);
            case Y:
                return (pd_ind==4 && cd_ind==2) ||
                        (pd_ind==5 && cd_ind==3) ||
                        (pd_ind==2 && cd_ind==5) ||
                        (pd_ind==3 && cd_ind==4);
            case Z:
                return (pd_ind==0 && cd_ind==4) ||
                        (pd_ind==1 && cd_ind==5) ||
                        (pd_ind==4 && cd_ind==1) ||
                        (pd_ind==5 && cd_ind==0);
        }
        return false;
    }


    public VoxelShape getAssociatedShape(){
        if (plane_val[0]>plane_val[3]){
            int pv0 = plane_val[0];
            plane_val[0] = plane_val[3];
            plane_val[3] = pv0;
        }
        if (plane_val[1]>plane_val[4]){
            int pv1 = plane_val[1];
            plane_val[1] = plane_val[4];
            plane_val[4] = pv1;
        }
        if (plane_val[2]>plane_val[5]){
            int pv2 = plane_val[2];
            plane_val[2] = plane_val[5];
            plane_val[5] = pv2;
        }
        return Block.box(
                plane_val[0], plane_val[1], plane_val[2],
                plane_val[3], plane_val[4], plane_val[5]);
    }
}
