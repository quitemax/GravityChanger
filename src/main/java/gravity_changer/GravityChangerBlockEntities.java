package gravity_changer;

import gravity_changer.block_entity.GravityPlatingBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

public class GravityChangerBlockEntities {

    public static BlockEntityType<@NotNull GravityPlatingBlockEntity> PLATING_ENTITY_TYPE;

    public static void init() {
        PLATING_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(
            GravityPlatingBlockEntity::new, GravityChangerBlocks.PLATING_BLOCK
        ).build();
        Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.parse("gravity_changer:plating_block_entity_type"),
            PLATING_ENTITY_TYPE
        );
    }
}