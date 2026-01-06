package gravity_changer;

import gravity_changer.block.GravityPlatingBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

public class GravityChangerBlocks {

    public static final GravityPlatingBlock PLATING_BLOCK = registerGravityPlatingBlock();

    public static void init() {
    }

    private static GravityPlatingBlock registerGravityPlatingBlock() {
        String name = "plating";
        ResourceKey<@NotNull Block> blockKey = keyOfBlock(name);

        BlockBehaviour.Properties settings = BlockBehaviour.Properties.of()
                .setId(blockKey)
                .sound(SoundType.GRASS)
                ;

        GravityPlatingBlock block = new GravityPlatingBlock(settings);

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    private static ResourceKey<@NotNull Block> keyOfBlock(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, name));
    }

}