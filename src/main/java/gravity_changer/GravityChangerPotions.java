package gravity_changer;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

public class GravityChangerPotions {
    public static final Holder<@NotNull Potion> STRENGTH_DECR_POTION =
        Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, "gravity_strength_decrease"),
            new Potion(
                "gravity_strength_decrease",
                new MobEffectInstance(
                    GravityChangerMobEffects.DECREASE,
                    9600,
                    0
                )
            )
        );

    public static final Holder<@NotNull Potion> STRENGTH_DECR_W_AMP_POTION =
        Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, "gravity_strength_decrease_with_amplification"),
            new Potion(
                "gravity_strength_decrease_with_amplification",
                new MobEffectInstance(
                    GravityChangerMobEffects.DECREASE,
                    9600,
                    1
                )
            )
        );

    public static final Holder<@NotNull Potion> STRENGTH_INCR_POTION =
        Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, "gravity_strength_increase"),
            new Potion(
                "gravity_strength_increase",
                new MobEffectInstance(
                    GravityChangerMobEffects.DECREASE,
                    9600,
                    0
                )
            )
        );

    public static final Holder<@NotNull Potion> STRENGTH_INCR_W_AMP_POTION =
        Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, "gravity_strength_increase_with_amplification"),
            new Potion(
                "gravity_strength_increase_with_amplification",
                new MobEffectInstance(
                    GravityChangerMobEffects.INCREASE,
                    9600,
                    1
                )
            )
        );
    public static final Holder<@NotNull Potion> STRENGTH_REVERSE_POTION =
            Registry.registerForHolder(
                    BuiltInRegistries.POTION,
                    Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, "gravity_strength_reverse"),
                    new Potion(
                            "gravity_strength_reverse",
                            new MobEffectInstance(
                                    GravityChangerMobEffects.DECREASE,
                                    9600,
                                    0
                            )
                    )
            );

    public static final Holder<@NotNull Potion> STRENGTH_REVERSE_W_AMP_POTION =
        Registry.registerForHolder(
            BuiltInRegistries.POTION,
            Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, "gravity_strength_reverse_with_amplification"),
            new Potion(
                "gravity_strength_reverse_with_amplification",
                new MobEffectInstance(
                    GravityChangerMobEffects.REVERSE,
                    9600,
                    1
                )
            )
        );

    public static final EnumMap<Direction, Holder<@NotNull Potion>> DIR_POTIONS = new EnumMap<>(Direction.class);
    
    static {
        for (Direction direction : Direction.values()) {
            Holder<@NotNull Potion> potion = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, "gravity_dir_" + direction.toString().toLowerCase()),
                new Potion(
                    "gravity_dir_" + direction.toString().toLowerCase(),
                    new MobEffectInstance(
                        GravityChangerMobEffects.DIR_EFFECT_MAP.get(direction),
                        9600,
                        1
                    )
                )
            );
            DIR_POTIONS.put(direction, potion);
        }
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(GravityChangerMod.GRAVITY_ITEM_GROUP_KEY)
            .register(itemGroup -> {
                Item[] potionItems = new Item[]{
                    Items.POTION,
                    Items.SPLASH_POTION,
                    Items.LINGERING_POTION
                };

                for (Item potionItem : potionItems) {
                    itemGroup.accept(PotionContents.createItemStack(potionItem, STRENGTH_DECR_POTION));
                    itemGroup.accept(PotionContents.createItemStack(potionItem, STRENGTH_DECR_W_AMP_POTION));
                    itemGroup.accept(PotionContents.createItemStack(potionItem, STRENGTH_INCR_W_AMP_POTION));
                    itemGroup.accept(PotionContents.createItemStack(potionItem, STRENGTH_INCR_W_AMP_POTION));
                    itemGroup.accept(PotionContents.createItemStack(potionItem, STRENGTH_REVERSE_POTION));
                    itemGroup.accept(PotionContents.createItemStack(potionItem, STRENGTH_REVERSE_W_AMP_POTION));

                    for (Direction direction : Direction.values()) {
                        itemGroup.accept(PotionContents.createItemStack(potionItem, DIR_POTIONS.get(direction)));
                    }
                }
            });
    }
}
