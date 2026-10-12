package gravity_changer.mixin;

import gravity_changer.api.GravityChangerAPI;
import gravity_changer.rotation.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

// Gravity strength and direction are handled in EntityMixin (getGravity/applyGravity).
// Here only the spawn position of thrown items is adjusted.
@Mixin(ThrowableItemProjectile.class)
public abstract class ThrowableProjectileMixin {
    @ModifyArgs(
        method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/throwableitemprojectile/ThrowableItemProjectile;<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)V",
            ordinal = 0
        )
    )
    private static void modifyargs_init_init_0(
        Args args, EntityType<? extends ThrowableItemProjectile> type, LivingEntity owner, Level world, ItemStack stack
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(owner);
        if (gravityDirection == Direction.DOWN) return;

        Vec3 pos = owner.getEyePosition().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.10000000149011612D, 0.0D, gravityDirection));
        args.set(1, pos.x);
        args.set(2, pos.y);
        args.set(3, pos.z);
    }
}
