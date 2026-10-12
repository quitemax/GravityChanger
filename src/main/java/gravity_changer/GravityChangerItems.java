package gravity_changer;

import gravity_changer.component.GravityComponent;
import gravity_changer.item.GravityAOEChangerItem;
import gravity_changer.item.GravityAnchorItem;
import gravity_changer.item.GravityChangerItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

public class GravityChangerItems {

    public static final GravityChangerItem GRAVITY_CHANGER_DOWN = registerChangerItem("gravity_changer_down", Direction.DOWN);
    public static final GravityChangerItem GRAVITY_CHANGER_UP = registerChangerItem("gravity_changer_up", Direction.UP);
    public static final GravityChangerItem GRAVITY_CHANGER_NORTH = registerChangerItem("gravity_changer_north", Direction.NORTH);
    public static final GravityChangerItem GRAVITY_CHANGER_SOUTH = registerChangerItem("gravity_changer_south", Direction.SOUTH);
    public static final GravityChangerItem GRAVITY_CHANGER_WEST = registerChangerItem("gravity_changer_west", Direction.WEST);
    public static final GravityChangerItem GRAVITY_CHANGER_EAST = registerChangerItem("gravity_changer_east", Direction.EAST);

    public static final EnumMap<Direction, GravityAnchorItem> ITEM_MAP = new EnumMap<>(Direction.class);
    static {
        for (Direction direction : Direction.values()) {
            ITEM_MAP.put(direction, registerAnchorItem("gravity_anchor_" + direction.getName(), direction));
        }
    }

    public static final GravityAOEChangerItem GRAVITY_AOE_CHANGER_DOWN = registerAOEChangerItem("gravity_aoe_changer_down", Direction.DOWN);
    public static final GravityAOEChangerItem GRAVITY_AOE_CHANGER_UP = registerAOEChangerItem("gravity_aoe_changer_up", Direction.UP);
    public static final GravityAOEChangerItem GRAVITY_AOE_CHANGER_NORTH = registerAOEChangerItem("gravity_aoe_changer_north", Direction.NORTH);
    public static final GravityAOEChangerItem GRAVITY_AOE_CHANGER_SOUTH = registerAOEChangerItem("gravity_aoe_changer_south", Direction.SOUTH);
    public static final GravityAOEChangerItem GRAVITY_AOE_CHANGER_WEST = registerAOEChangerItem("gravity_aoe_changer_west", Direction.WEST);
    public static final GravityAOEChangerItem GRAVITY_AOE_CHANGER_EAST = registerAOEChangerItem("gravity_aoe_changer_east", Direction.EAST);

    public static void init() {
        registerGravityItemsWithGravityItemGroup();
        registerGravityUpdateEventForAnchorItemInHand();
    }

    public static void registerGravityItemsWithGravityItemGroup() {
        CreativeModeTabEvents.modifyOutputEvent(GravityChangerMod.GRAVITY_ITEM_GROUP_KEY)
            .register(itemGroup -> {
                itemGroup.accept(GRAVITY_CHANGER_DOWN);
                itemGroup.accept(GRAVITY_CHANGER_UP);
                itemGroup.accept(GRAVITY_CHANGER_EAST);
                itemGroup.accept(GRAVITY_CHANGER_WEST);
                itemGroup.accept(GRAVITY_CHANGER_NORTH);
                itemGroup.accept(GRAVITY_CHANGER_SOUTH);

                itemGroup.accept(GRAVITY_AOE_CHANGER_DOWN);
                itemGroup.accept(GRAVITY_AOE_CHANGER_UP);
                itemGroup.accept(GRAVITY_AOE_CHANGER_EAST);
                itemGroup.accept(GRAVITY_AOE_CHANGER_WEST);
                itemGroup.accept(GRAVITY_AOE_CHANGER_NORTH);
                itemGroup.accept(GRAVITY_AOE_CHANGER_SOUTH);

                for (GravityAnchorItem item : ITEM_MAP.values()) {
                    itemGroup.accept(item);
                }
            });
    }

    public static void registerGravityUpdateEventForAnchorItemInHand() {
        GravityComponent.GRAVITY_UPDATE_EVENT.register((entity, component) -> {
            ItemStack handSlot = entity.getWeaponItem();
            if (handSlot == null) {
                // non-living entities have no weapon item
                return;
            }
            Item item = handSlot.getItem();
            if (item instanceof GravityAnchorItem anchorItem) {
                component.applyGravityEffectDirection(
                        anchorItem.gravityDirection,
                        null,
                        1000000
                );
            }
        });
    }

    public static GravityChangerItem registerChangerItem(String name, Direction gravityDirection) {
        ResourceKey<@NotNull Item> itemKey = getItemResourceKey(name);
        Item.Properties settings = getItemDefaultPropertiesWith(itemKey);

        settings
            .component(
                DataComponents.LORE,
                new ItemLore(
                    List.of(
                        Component.translatable("gravity_changer.gravity_changer.tooltip.0").withStyle(ChatFormatting.GRAY),
                        Component.translatable("gravity_changer.gravity_changer.tooltip.1").withStyle(ChatFormatting.GRAY)
                    )
                )
            );

        GravityChangerItem item = new GravityChangerItem(settings, gravityDirection);
        Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                item
        );

        return item;
    }

    public static GravityAOEChangerItem registerAOEChangerItem(String name, Direction gravityDirection) {
        ResourceKey<@NotNull Item> itemKey = getItemResourceKey(name);
        Item.Properties settings = getItemDefaultPropertiesWith(itemKey);
        GravityAOEChangerItem item = new GravityAOEChangerItem(settings, gravityDirection);
        Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                item
        );

        return item;
    }

    public static GravityAnchorItem registerAnchorItem(String name, Direction gravityDirection) {
        ResourceKey<@NotNull Item> itemKey = getItemResourceKey(name);
        Item.Properties settings = getItemDefaultPropertiesWith(itemKey);
        settings
            .component(
                DataComponents.LORE,
                new ItemLore(
                        List.of(
                                Component.translatable("gravity_changer.gravity_anchor.tooltip.0").withStyle(ChatFormatting.GRAY),
                                Component.translatable("gravity_changer.gravity_anchor.tooltip.1").withStyle(ChatFormatting.GRAY)
                        )
                )
            );
        GravityAnchorItem item = new GravityAnchorItem(settings, gravityDirection);
        Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                item
        );

        return item;
    }

    public static ResourceKey<@NotNull Item> getItemResourceKey(String name) {
        return ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, name)
        );
    }

    public static Item.Properties getItemDefaultPropertiesWith(ResourceKey<@NotNull Item> itemKey) {
        return new Item.Properties()
                .stacksTo(1)
                .setId(itemKey);
    }
}