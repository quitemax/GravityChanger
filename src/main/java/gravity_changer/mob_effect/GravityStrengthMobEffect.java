package gravity_changer.mob_effect;

import gravity_changer.component.GravityComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class GravityStrengthMobEffect extends MobEffect {
    
    public final double base;
    public final int signum;

    public GravityStrengthMobEffect(int color, double base, int signum) {
        super(MobEffectCategory.NEUTRAL, color);
        this.base = base;
        this.signum = signum;
    }
    
    public double getGravityStrengthMultiplier(int level) {
        return Math.pow(base, level) * signum;
    }

    public void apply(LivingEntity entity, GravityComponent component) {
        MobEffectInstance effectInstance = entity.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this));
        if (effectInstance == null) {
            return;
        }
        
        int level = effectInstance.getAmplifier() + 1;
        component.applyGravityEffectStrength(getGravityStrengthMultiplier(level));
    }

//    // Called every tick to check if the effect can be applied or not
//    @Override
//    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
//        // In our case, we just make it return true so that it applies the effect every tick
//        return true;
//    }
//
//    // Called when the effect is applied.
//    @Override
//    public boolean applyEffectTick(@NotNull ServerLevel world, @NotNull LivingEntity entity, int amplifier) {
//        if (entity instanceof Player) {
////            ((Player) entity).giveExperiencePoints(1 << amplifier); // Higher amplifier gives you experience faster
//        }
//
//        return super.applyEffectTick(world, entity, amplifier);
//    }
}
