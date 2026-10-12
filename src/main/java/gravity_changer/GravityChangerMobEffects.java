package gravity_changer;

import gravity_changer.component.GravityComponent;
import gravity_changer.mob_effect.GravityDirectionMobEffect;
import gravity_changer.mob_effect.GravityInvertMobEffect;
import gravity_changer.mob_effect.GravityStrengthMobEffect;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

public class GravityChangerMobEffects {

    public static final Identifier MOB_EFFECT_INVERT_PHASE = Identifier.parse("gravity_changer:invert_mob_effect_phase");
    public static final Identifier MOB_EFFECT_DIRECTION_PHASE = Identifier.parse("gravity_changer:dir_mob_effect_phase");
    public static final Identifier MOB_EFFECT_INVERT = Identifier.parse("gravity_changer:invert");

    //
    public static final Holder<@NotNull MobEffect> INCREASE =
        Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, "strength_increase"),
            new GravityStrengthMobEffect(0x98D982, 1.2, 1)
        );

    public static final Holder<@NotNull MobEffect> DECREASE =
        Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, "strength_decrease"),
            new GravityStrengthMobEffect(0x98D982, 0.7, 1)
        );

    public static final Holder<@NotNull MobEffect> REVERSE =
        Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, "strength_reverse"),
            new GravityStrengthMobEffect(0x98D982, 1.0, -1)
        );

    //
    public static final EnumMap<Direction, Holder<@NotNull MobEffect>> DIR_EFFECT_MAP =
        new EnumMap<>(Direction.class);

    static {
        for (Direction dir : Direction.values()) {
            Holder<@NotNull MobEffect> effect = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                Identifier.fromNamespaceAndPath(GravityChangerMod.MOD_ID, "dir_" + dir.toString().toLowerCase()),
                new GravityDirectionMobEffect(dir)
            );
            DIR_EFFECT_MAP.put(dir, effect);
        }
    }

    //
    public static final Holder<@NotNull MobEffect> INVERT =
        Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            MOB_EFFECT_INVERT,
            new GravityInvertMobEffect()
        );

    public static void init() {

        GravityComponent.GRAVITY_UPDATE_EVENT.register(
            MOB_EFFECT_DIRECTION_PHASE, (entity, component) -> {
                if (!(entity instanceof LivingEntity livingEntity)) {
                    return;
                }

                for (Holder<@NotNull MobEffect> dirEffect : GravityChangerMobEffects.DIR_EFFECT_MAP.values()) {
                    MobEffectInstance effectInstance = livingEntity.getEffect( dirEffect);
                    if (effectInstance != null) {
                        int amplifier = effectInstance.getAmplifier();

                        component.applyGravityEffectDirection(
                            ((GravityDirectionMobEffect) dirEffect.value()).gravityDirection,
                            null,
                            amplifier + 1.0
                        );
                    }
                }
            }
        );

        GravityComponent.GRAVITY_UPDATE_EVENT.register(
            MOB_EFFECT_INVERT_PHASE, (entity, component) -> {
                if (entity instanceof LivingEntity livingEntity) {
                    if (livingEntity.hasEffect(INVERT)) {
                        component.applyGravityEffectDirection(
                                component.getCurrGravityDirection().getOpposite(),
                                null,
                                5
                        );
                    }
                }
            }
        );

        // apply invert after gravity effect
        GravityComponent.GRAVITY_UPDATE_EVENT.addPhaseOrdering(
                MOB_EFFECT_DIRECTION_PHASE,
                MOB_EFFECT_INVERT_PHASE
        );

        GravityComponent.GRAVITY_UPDATE_EVENT.register((entity, component) -> {
            if (entity instanceof LivingEntity livingEntity) {
                ((GravityStrengthMobEffect)INCREASE.value()).apply(livingEntity, component);
                ((GravityStrengthMobEffect)DECREASE.value()).apply(livingEntity, component);
                ((GravityStrengthMobEffect)REVERSE.value()).apply(livingEntity, component);
            }
        });

    }
}