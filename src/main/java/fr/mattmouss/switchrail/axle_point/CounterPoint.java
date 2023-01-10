package fr.mattmouss.switchrail.axle_point;

import fr.mattmouss.switchrail.other.Util;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

// a unique counting point (WARNING : work in both direction if bidirectional is true)
public class CounterPoint {
    private final BlockPos acPos ;
    private final Config config;

    public CounterPoint(BlockPos pos,Config config){
         this.acPos = pos;
         this.config = config;
    }

    public CounterPoint(BlockPos pos, Direction direction, boolean addAxle, boolean fromOutside, boolean bidirectional){
        this.acPos = pos;
        this.config = new Config(direction, addAxle, fromOutside, bidirectional);
    }

    public BlockPos getACPos(){
        return acPos;
    }

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

    public boolean test(BlockPos pos,Direction side){
        return this.getACPos().equals(pos) && this.getCountingDirection().equals(side);
    }

    public CounterPoint(PacketBuffer buf){
        this.acPos = buf.readBlockPos();
        this.config = new Config(buf);
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(acPos);
        config.toBytes(buf);
    }

    public INBT write(){
        CompoundNBT nbt = new CompoundNBT();
        Util.putPos(nbt,acPos);
        config.write(nbt);
        return nbt;
    }

    public static CounterPoint read(INBT nbt){
        CompoundNBT nbt1 = (CompoundNBT) nbt;
        BlockPos pos = Util.getPosFromNbt(nbt1);
        Config config = Config.read(nbt1);
        return new CounterPoint(pos,config);
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

        public Config(PacketBuffer buf){
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

        public void toBytes(PacketBuffer buf) {
            buf.writeEnum(direction);
            buf.writeBoolean(addAxle);
            buf.writeBoolean(fromOutside);
            buf.writeBoolean(bidirectional);
        }

        public void write(CompoundNBT nbt) {
            nbt.putByte("direction", (byte) direction.get2DDataValue());
            nbt.putBoolean("add_axle",addAxle);
            nbt.putBoolean("from_outside",fromOutside);
            nbt.putBoolean("bidirectional",bidirectional);
        }

        public static Config read(CompoundNBT nbt){
            Direction direction = Direction.from2DDataValue(nbt.getByte("direction"));
            boolean addAxle = nbt.getBoolean("add_axle");
            boolean fromOutside = nbt.getBoolean("from_outside");
            boolean bidirectional = nbt.getBoolean("bidirectional");
            return new Config(direction,addAxle,fromOutside,bidirectional);
        }

    }


}
