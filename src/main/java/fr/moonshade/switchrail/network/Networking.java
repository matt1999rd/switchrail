package fr.moonshade.switchrail.network;

import fr.moonshade.switchrail.SwitchRailMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Networking {
    public static SimpleChannel INSTANCE;
    private static final String PROTOCOL_VERSION = "1";
    private static int ID =0;

    public static int nextID(){ return ID++; }

    public static void registerMessages(){
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(SwitchRailMod.MOD_ID,"switchrail"),()->PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        INSTANCE.registerMessage(nextID(),
                ChangePosPacket.class,
                ChangePosPacket::toBytes,
                ChangePosPacket::new,
                ChangePosPacket::handle);

        INSTANCE.registerMessage(nextID(),
                ChangeSwitchPacket.class,
                ChangeSwitchPacket::toBytes,
                ChangeSwitchPacket::new,
                ChangeSwitchPacket::handle);

        INSTANCE.registerMessage(nextID(),
                TerminalScreenPacket.class,
                TerminalScreenPacket::toBytes,
                TerminalScreenPacket::new,
                TerminalScreenPacket::handle);

        INSTANCE.registerMessage(nextID(),
                OpenControllerScreenPacket.class,
                OpenControllerScreenPacket::toBytes,
                OpenControllerScreenPacket::new,
                OpenControllerScreenPacket::handle);

        INSTANCE.registerMessage(nextID(),
                OpenTerminalScreenPacket.class,
                OpenTerminalScreenPacket::toBytes,
                OpenTerminalScreenPacket::new,
                OpenTerminalScreenPacket::handle);

        INSTANCE.registerMessage(nextID(),
                OpenCounterScreenPacket.class,
                OpenCounterScreenPacket::toBytes,
                OpenCounterScreenPacket::new,
                OpenCounterScreenPacket::handle);

        INSTANCE.registerMessage(nextID(),
                ActionOnTilePacket.class,
                ActionOnTilePacket::toBytes,
                ActionOnTilePacket::new,
                ActionOnTilePacket::handle);

        INSTANCE.registerMessage(nextID(),
                UpdateCounterPointPacket.class,
                UpdateCounterPointPacket::toBytes,
                UpdateCounterPointPacket::new,
                UpdateCounterPointPacket::handle);

        INSTANCE.registerMessage(nextID(),
                SetAxleNumberPacket.class,
                SetAxleNumberPacket::toBytes,
                SetAxleNumberPacket::new,
                SetAxleNumberPacket::handle);

        INSTANCE.registerMessage(nextID(),
                ChangeZoomPacket.class,
                ChangeZoomPacket::toBytes,
                ChangeZoomPacket::new,
                ChangeZoomPacket::handle);

    }





}
