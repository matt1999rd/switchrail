package fr.moonshade.switchrail.setup;

import fr.moonshade.switchrail.switchblock.Switch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.Arrays;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UpdateSwitchRailHandler {
    public static class WorldLoadingHandler {
        public WorldLoadingHandler() {
        }

        public boolean isOldVersion(ChunkEvent.Load event){
            ServerLevel level = (ServerLevel) event.getWorld();
            WorldData levelData = level.getServer().getWorldData();
            if (levelData instanceof PrimaryLevelData primaryLevelData){
                try {
                    Field field = PrimaryLevelData.class.getDeclaredField("playerDataVersion");
                    field.setAccessible(true);
                    int version = (int) field.get(primaryLevelData);
                    if (version > 2724)return false;
                }catch (NoSuchFieldException | IllegalAccessException e) {
                    System.out.println("Failed in searching version : version will be considered newer or equal to 1.18.1 ");
                    System.out.println("Error send is as followed : ");
                    System.out.println(e.getMessage());
                    return false;
                }
            }
            return true;
        }

        public void removeWaterFromSwitch(LevelChunkSection section){
            for (int x = 0; x < LevelChunkSection.SECTION_WIDTH;x++){
                for (int y = 0; y < LevelChunkSection.SECTION_HEIGHT;y++){
                    for (int z = 0; z < LevelChunkSection.SECTION_WIDTH; z++){
                        BlockState state = section.getBlockState(x,y,z);
                        if (state.getBlock() instanceof Switch){
                            System.out.println("Set Switch to WATERLOGGED false...");
                            section.setBlockState(x,y,z,state.setValue(BlockStateProperties.WATERLOGGED,false));
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public void onLoadChunk(ChunkEvent.Load event) {
            if (!event.getWorld().isClientSide()){
                System.out.println("Do chunk update. Version check...");
                if (!isOldVersion(event)){
                    System.out.println("Version newer than 1.18.2 skip action !");
                    return;
                }
                System.out.println("Version older than 1.18.2 : make update");
                System.out.println("Starting switch correction...");

                ChunkAccess chunkAccess = event.getChunk();
                Arrays.stream(chunkAccess.getSections()).forEach(this::removeWaterFromSwitch);
            }
        }

    }
}
