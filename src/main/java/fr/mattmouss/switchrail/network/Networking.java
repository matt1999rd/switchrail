package fr.mattmouss.gates.network;

import fr.mattmouss.gates.GatesMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Networking {
    public static SimpleChannel INSTANCE;
    private static int ID =0;

    public static int nextID(){ return ID++; }

    public static void registerMessages(){
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(GatesMod.MODID,"gates"),()->"1.0",s -> true,s -> true);

        INSTANCE.registerMessage(nextID(),
                PacketLowerPrice.class,
                PacketLowerPrice::toBytes,
                PacketLowerPrice::new,
                PacketLowerPrice::handle);

        INSTANCE.registerMessage(nextID(),
                PacketRaisePrice.class,
                PacketRaisePrice::toBytes,
                PacketRaisePrice::new,
                PacketRaisePrice::handle);
        INSTANCE.registerMessage(nextID(),
                ChangeIdPacket.class,
                ChangeIdPacket::toBytes,
                ChangeIdPacket::new,
                ChangeIdPacket::handle);

        INSTANCE.registerMessage(nextID(),
                SetIdPacket.class,
                SetIdPacket::toBytes,
                SetIdPacket::new,
                SetIdPacket::handle);

        INSTANCE.registerMessage(nextID(),
                ChangeClientIdPacket.class,
                ChangeClientIdPacket::toBytes,
                ChangeClientIdPacket::new,
                ChangeClientIdPacket::handle);

    }





}
