package gravity_changer;


import gravity_changer.block_entity.GravityPlatingBlockEntity;
import gravity_changer.block_item.GravityPlatingBlockItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import java.util.List;

public class GravityChangerBlockItems {

    public static final GravityPlatingBlockItem PLATING_BLOCK_ITEM = registerPlatingBlockItem();

    private static GravityPlatingBlockItem registerPlatingBlockItem() {
        Block block = GravityChangerBlocks.PLATING_BLOCK;
        String name = "plating";

        ResourceKey<@NotNull Item> itemKey = keyOfItem(name);

        Item.Properties properties = new Item.Properties();
        properties
            .setId(itemKey)
            .useBlockDescriptionPrefix()
            .component(
                DataComponents.LORE,
                new ItemLore(
                    List.of(
                        Component.translatable("item.gravity_changer.plating.tooltip.0").withStyle(ChatFormatting.GRAY),
                        Component.translatable("item.gravity_changer.plating.tooltip.1").withStyle(ChatFormatting.GRAY),
                        Component.translatable("item.gravity_changer.plating.tooltip.2").withStyle(ChatFormatting.GRAY)
                    )
                )
            );

        GravityPlatingBlockItem blockItem = new GravityPlatingBlockItem(block, properties);
        return Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
    }

    private static ResourceKey<@NotNull Item> keyOfItem(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, name));
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(GravityChangerMod.GRAVITY_ITEM_GROUP_KEY)
                .register(itemGroup -> {
                    itemGroup.accept(createPlatingBlockItemStack(new GravityPlatingBlockEntity.PlatingBlockSideState(true, 1)));
                    itemGroup.accept(createPlatingBlockItemStack(new GravityPlatingBlockEntity.PlatingBlockSideState(true, 2)));
                    itemGroup.accept(createPlatingBlockItemStack(new GravityPlatingBlockEntity.PlatingBlockSideState(true, 4)));
                    itemGroup.accept(createPlatingBlockItemStack(new GravityPlatingBlockEntity.PlatingBlockSideState(true, 8)));
                    itemGroup.accept(createPlatingBlockItemStack(new GravityPlatingBlockEntity.PlatingBlockSideState(true, 16)));
                    itemGroup.accept(createPlatingBlockItemStack(new GravityPlatingBlockEntity.PlatingBlockSideState(true, 32)));
                    itemGroup.accept(createPlatingBlockItemStack(new GravityPlatingBlockEntity.PlatingBlockSideState(true, 64)));
                    itemGroup.accept(createPlatingBlockItemStack(new GravityPlatingBlockEntity.PlatingBlockSideState(false, 8)));
                    itemGroup.accept(createPlatingBlockItemStack(new GravityPlatingBlockEntity.PlatingBlockSideState(false, 16)));
                    itemGroup.accept(createPlatingBlockItemStack(new GravityPlatingBlockEntity.PlatingBlockSideState(false, 64)));
                });
    }

    public static ItemStack createPlatingBlockItemStack(GravityPlatingBlockEntity.PlatingBlockSideState sideState) {
        ItemStack itemStack = new ItemStack(PLATING_BLOCK_ITEM);

        CompoundTag blockEntityData = new CompoundTag();
        assert sideState != null;

        blockEntityData.putBoolean("isAttracting", sideState.isAttracting);
        blockEntityData.putInt("level", sideState.level);

        itemStack.set(
            DataComponents.BLOCK_ENTITY_DATA,
            TypedEntityData.of(GravityChangerBlockEntities.PLATING_ENTITY_TYPE, blockEntityData)
        );

        Component text = Component.translatable(
            sideState.isAttracting
                ? "gravity_changer.plate.force.attract"
                : "gravity_changer.plate.force.repulse"
        );
        itemStack.set(
            DataComponents.CUSTOM_NAME,
            Component.translatable("item.gravity_changer.plating.item_name", sideState.level, text)
        );

        return itemStack;
    }
}