package fr.mattmouss.switchrail;




import fr.mattmouss.switchrail.blocks.*;
import fr.mattmouss.switchrail.gui.ControllerContainer;
import fr.mattmouss.switchrail.item.SwitchLever;
import fr.mattmouss.switchrail.item.SwitchRegister;
import fr.mattmouss.switchrail.other.SwitchStorageCapability;
import fr.mattmouss.switchrail.switchblock.*;
import fr.mattmouss.switchrail.setup.ClientProxy;
import fr.mattmouss.switchrail.setup.IProxy;

import fr.mattmouss.switchrail.setup.ServerProxy;
import net.minecraft.block.Block;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.logging.Logger;

@Mod(SwitchRailMod.MODID)
public class SwitchRailMod {
    //2400 lignes de code enjoy !!!!

    public static IProxy proxy = DistExecutor.runForDist(()->()-> new ClientProxy(),()->()->new ServerProxy());

    public static final String MODID = "switchrail";

    //public static ModSetup setup = new ModSetup();

    public static final Logger logger =  Logger.getLogger(MODID);

    public SwitchRailMod (){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);

        MinecraftForge.EVENT_BUS.register(this);

    }

    public void preInit(FMLCommonSetupEvent evt) {
        SwitchStorageCapability.register();

    }

    private void setup(final FMLCommonSetupEvent event) {
        //setup.init();

        proxy.init();

    }


    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)

    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            blockRegistryEvent.getRegistry().register(new SwitchStraightNLeft());
            blockRegistryEvent.getRegistry().register(new SwitchStraightNRight());
            blockRegistryEvent.getRegistry().register(new SwitchStraightVLeft());
            blockRegistryEvent.getRegistry().register(new SwitchStraightVRight());
            blockRegistryEvent.getRegistry().register(new SwitchDoubleTurn());
            blockRegistryEvent.getRegistry().register(new CrossedRail());
            blockRegistryEvent.getRegistry().register(new SwitchTriple());
            blockRegistryEvent.getRegistry().register(new Switch_Tjs());
            blockRegistryEvent.getRegistry().register(new Switch_Tjd());
            blockRegistryEvent.getRegistry().register(new ControllerBlock());
            blockRegistryEvent.getRegistry().register(new Bumper());
            blockRegistryEvent.getRegistry().register(new OneWayPoweredRail());
        }


        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> blockRegistryEvent) {
            Item.Properties properties = new Item.Properties().group(ItemGroup.TRANSPORTATION);
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.switchStraightNLeft,properties).setRegistryName("switch_n_left"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.switchStraightNRight,properties).setRegistryName("switch_n_right"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.switchStraightVLeft,properties).setRegistryName("switch_v_left"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.switchStraightVRight,properties).setRegistryName("switch_v_right"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.SWITCH_DOUBLE_TURN,properties).setRegistryName("double_turn_switch"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.CROSSED_RAIL,properties).setRegistryName("crossed_rail"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.TRIPLE_SWITCH,properties).setRegistryName("triple_switch"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.SWITCH_TJS,properties).setRegistryName("switch_tjs"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.SWITCH_TJD,properties).setRegistryName("switch_tjd"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.CONTROLLER_BLOCK,properties).setRegistryName("controller_block"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.BUMPER,properties).setRegistryName("bumper"));
            blockRegistryEvent.getRegistry().register(new SwitchLever(properties.maxStackSize(1)));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.ONE_WAY_POWERED_RAIL,properties.maxStackSize(64)).setRegistryName("one_way_powered_rail"));
            //blockRegistryEvent.getRegistry().register(new SwitchRegister(properties)); // si on veut controler les aiguilles




        }

        @SubscribeEvent

        public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event)
        {
            event.getRegistry().register(TileEntityType.Builder.create(SwitchTile::new,ModBlock.switchStraightNLeft,
                    ModBlock.switchStraightNRight,
                    ModBlock.switchStraightVLeft,
                    ModBlock.switchStraightVRight,
                    ModBlock.SWITCH_DOUBLE_TURN,
                    ModBlock.SWITCH_TJD,
                    ModBlock.SWITCH_TJS,
                    ModBlock.TRIPLE_SWITCH).build(null).setRegistryName("switch"));
            event.getRegistry().register(TileEntityType.Builder.create(ControllerTile::new,ModBlock.CONTROLLER_BLOCK)
                    .build(null).setRegistryName("controller_block"));
            event.getRegistry().register(TileEntityType.Builder.create(BumperTile::new,ModBlock.BUMPER)
                    .build(null).setRegistryName("bumper"));


        }
        @SubscribeEvent
        public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> event){
            event.getRegistry().register(IForgeContainerType.create(((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                return new ControllerContainer(windowId,
                        SwitchRailMod.proxy.getClientWorld(),
                        pos,
                        SwitchRailMod.proxy.getClientPlayer());
            })).setRegistryName("controller_block"));
        }
        /*
        @SubscribeEvent

        public static void onSoundEventRegistry(final RegistryEvent.Register<SoundEvent> event){
            //event.getRegistry().register(proxy.);
        }

         */





    }
}
