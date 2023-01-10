package fr.mattmouss.switchrail.enum_rail;

import fr.mattmouss.switchrail.gui.ControllerScreen;
import fr.mattmouss.switchrail.gui.CounterScreen;
import fr.mattmouss.switchrail.gui.RailScreen;
import fr.mattmouss.switchrail.gui.TerminalScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import java.lang.reflect.InvocationTargetException;

public enum ScreenType {
    CONTROLLER(0, ControllerScreen.class),
    TERMINAL(1, TerminalScreen.class),
    COUNTER(2, CounterScreen.class);

    final int meta;
    final Class<? extends RailScreen> screenClass;
    ScreenType(int meta,Class<? extends RailScreen> screenClass){
        this.meta = meta;
        this.screenClass = screenClass;
    }

    public static ScreenType readBuf(PacketBuffer buf){
        ScreenType[] types = ScreenType.values();
        byte b = buf.readByte();
        if (b>-1 && b<3){
            return types[b];
        }else {
            throw new IllegalArgumentException("Error in transmission Packet : index is incorrect : "+b+". Expect 0, 1 or 2.");
        }
    }

    public void write(PacketBuffer buf){
        buf.writeByte(this.meta);
    }

    public void open(BlockPos te_pos) {
        try {
            Minecraft.getInstance().setScreen(screenClass.getConstructor(BlockPos.class).newInstance(te_pos));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e){
            e.printStackTrace();
        }
    }
}
