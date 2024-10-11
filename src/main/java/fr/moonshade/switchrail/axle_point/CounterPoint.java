package fr.moonshade.switchrail.axle_point;

import fr.moonshade.switchrail.other.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

// a unique counting point (WARNING : work in both direction if bidirectional is true)
public class CounterPoint {
    private final BlockPos acPos;
    private final int index;  // -1 if no index is given. Bad usage to my mind but optional is forbidden
    private final Config config;

    public CounterPoint(BlockPos pos,Config config,int index){
         this.acPos = pos;
         this.config = config;
         this.index = index;
    }

    public CounterPoint(BlockPos pos,int index, Direction direction, boolean addAxle, boolean fromOutside, boolean bidirectional){
        this.acPos = pos;
        this.config = new Config(direction, addAxle, fromOutside, bidirectional);
        this.index = index;
    }

    public BlockPos getACPos(){
        return acPos;
    }

    public boolean testPos(BlockPos acPos,int index){
        return this.acPos.equals(acPos) && (this.index == index);
    }

    public int getIndex(){ return index; }

    public Direction getCountingDirection(){
        return config.getCountingDirection();
    }

    public boolean isAddingAxle() { return config.isAddingAxle(); }

    public boolean countIfMinecartArrive(){ return config.countIfMinecartArrive(); }

    public boolean isBidirectional(){ return config.isBidirectional(); }

    public void toggleDirection(){
        config.toggleDirection();
    }

    public void toggleCounting(){
        config.toggleCounting();
    }

    public void toggleBidirectional() { config.toggleBidirectional(); }

    public boolean test(BlockPos pos,Direction side,int index){
        return this.testPos(pos,index) && this.getCountingDirection().equals(side);
    }

    public CounterPoint(FriendlyByteBuf buf){
        this.acPos = buf.readBlockPos();
        this.config = new Config(buf);
        this.index = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(acPos);
        config.toBytes(buf);
        buf.writeInt(index);
    }

    public Tag write(){
        CompoundTag nbt = new CompoundTag();
        Util.putPos(nbt,acPos);
        config.write(nbt);
        nbt.putInt("index",index);
        return nbt;
    }

    public static CounterPoint read(Tag nbt){
        CompoundTag nbt1 = (CompoundTag) nbt;
        BlockPos pos = Util.getPosFromNbt(nbt1);
        Config config = Config.read(nbt1);
        int index;
        if (nbt1.contains("index")){
            index = nbt1.getInt("index");
        }else {
            index = -1;
        }
        return new CounterPoint(pos,config,index);
    }

    public static class Config{
        private final Direction direction ;
        private boolean addAxle ;
        private boolean fromOutside ;
        private boolean bidirectional;

        public Config(Direction direction, boolean addAxle, boolean fromOutside,boolean bidirectional){
            this.direction = direction;
            this.addAxle = addAxle;
            this.fromOutside = fromOutside;
            this.bidirectional = bidirectional;
        }

        public Config(FriendlyByteBuf buf){
            this.direction = buf.readEnum(Direction.class);
            this.addAxle = buf.readBoolean();
            this.fromOutside = buf.readBoolean();
            this.bidirectional = buf.readBoolean();
        }

        public Direction getCountingDirection(){
            return direction;
        }

        public boolean isAddingAxle() { return addAxle; }

        public boolean countIfMinecartArrive(){ return fromOutside; }

        public boolean isBidirectional() { return bidirectional; }

        public void toggleDirection(){
            this.fromOutside = !fromOutside;
        }

        public void toggleCounting(){
            this.addAxle = !addAxle;
        }

        public void toggleBidirectional() { this.bidirectional = !bidirectional; }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeEnum(direction);
            buf.writeBoolean(addAxle);
            buf.writeBoolean(fromOutside);
            buf.writeBoolean(bidirectional);
        }

        public void write(CompoundTag nbt) {
            nbt.putByte("direction", (byte) direction.get2DDataValue());
            nbt.putBoolean("add_axle",addAxle);
            nbt.putBoolean("from_outside",fromOutside);
            nbt.putBoolean("bidirectional",bidirectional);
        }

        public static Config read(CompoundTag nbt){
            Direction direction = Direction.from2DDataValue(nbt.getByte("direction"));
            boolean addAxle = nbt.getBoolean("add_axle");
            boolean fromOutside = nbt.getBoolean("from_outside");
            boolean bidirectional = nbt.getBoolean("bidirectional");
            return new Config(direction,addAxle,fromOutside,bidirectional);
        }

    }


}
