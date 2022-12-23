package fr.mattmouss.switchrail;




import fr.mattmouss.switchrail.blocks.*;
import fr.mattmouss.switchrail.item.SwitchLever;
import fr.mattmouss.switchrail.other.PosStorageCapability;
import fr.mattmouss.switchrail.setup.*;
import fr.mattmouss.switchrail.switchblock.*;

import net.minecraft.block.Block;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SwitchRailMod.MOD_ID)
public class SwitchRailMod {

    public static IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static final String MOD_ID = "switchrail";

    public static ModSetup setup = new ModSetup();

    public SwitchRailMod (){

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::setup);


        MinecraftForge.EVENT_BUS.register(this);

    }

    public void preInit(FMLCommonSetupEvent evt) {
        PosStorageCapability.register();

    }

    private void setup(final FMLCommonSetupEvent event) {
        setup.init();

        proxy.init();

    }


    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)

    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            blockRegistryEvent.getRegistry().register(new SwitchStraight());
            blockRegistryEvent.getRegistry().register(new SwitchDoubleTurn());
            blockRegistryEvent.getRegistry().register(new CrossedRail());
            blockRegistryEvent.getRegistry().register(new SwitchTriple());
            blockRegistryEvent.getRegistry().register(new SwitchSimpleSlip());
            blockRegistryEvent.getRegistry().register(new SwitchDoubleSlip());
            blockRegistryEvent.getRegistry().register(new ControllerBlock());
            blockRegistryEvent.getRegistry().register(new Bumper());
            blockRegistryEvent.getRegistry().register(new OneWayPoweredRail());
            blockRegistryEvent.getRegistry().register(new SwitchTerminal());
            blockRegistryEvent.getRegistry().register(new OneWayDetectorRail());
        }


        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> blockRegistryEvent) {
            Item.Properties properties = new Item.Properties().tab(ItemGroup.TAB_TRANSPORTATION);
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.SWITCH_STRAIGHT,properties).setRegistryName("switch_straight"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.SWITCH_DOUBLE_TURN,properties).setRegistryName("double_turn_switch"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.CROSSED_RAIL,properties).setRegistryName("crossed_rail"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.TRIPLE_SWITCH,properties).setRegistryName("triple_switch"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.SWITCH_TJS,properties).setRegistryName("switch_tjs"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.SWITCH_TJD,properties).setRegistryName("switch_tjd"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.CONTROLLER_BLOCK,properties).setRegistryName("controller_block"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.BUMPER,properties).setRegistryName("bumper"));
            blockRegistryEvent.getRegistry().register(new SwitchLever(properties.stacksTo(1)));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.ONE_WAY_POWERED_RAIL,properties.stacksTo(64).rarity(Rarity.COMMON)).setRegistryName("one_way_powered_rail"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.SWITCH_TERMINAL,properties).setRegistryName("switch_terminal"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.ONE_WAY_DETECTOR_RAIL,properties.stacksTo(64).rarity(Rarity.COMMON)).setRegistryName("one_way_detector_rail"));



        }

        @SubscribeEvent
        public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event)
        {
            event.getRegistry().register(TileEntityType.Builder.of(ControllerTile::new,ModBlock.CONTROLLER_BLOCK)
                    .build(null).setRegistryName("controller_block"));
            event.getRegistry().register(TileEntityType.Builder.of(BumperTile::new,ModBlock.BUMPER)
                    .build(null).setRegistryName("bumper"));
            event.getRegistry().register(TileEntityType.Builder.of(TerminalTile::new,ModBlock.SWITCH_TERMINAL)
                    .build(null).setRegistryName("switch_terminal"));
        }





    }
}
