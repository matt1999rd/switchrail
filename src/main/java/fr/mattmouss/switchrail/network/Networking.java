package fr.mattmouss.switchrail.network;

import fr.mattmouss.switchrail.SwitchRailMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Networking {
    public static SimpleChannel INSTANCE;
    private static int ID =0;

    public static int nextID(){ return ID++; }

    public static void registerMessages(){
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(SwitchRailMod.MODID,"switchrail"),()->"1.0", s -> true, s -> true);

        INSTANCE.registerMessage(nextID(),
                ChangePosPacket.class,
                ChangePosPacket::toBytes,
                ChangePosPacket::new,
                ChangePosPacket::handle);

    }





}
