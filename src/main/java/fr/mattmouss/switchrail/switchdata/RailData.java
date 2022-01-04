package fr.mattmouss.switchrail.switchdata;

import fr.mattmouss.switchrail.enum_rail.RailType;
import net.minecraft.util.math.BlockPos;

public class RailData {

    public RailType type;
    public BlockPos pos;

    public RailData(RailType type_in, BlockPos pos_in) {
        type =type_in;
        pos = pos_in;
    }



}
