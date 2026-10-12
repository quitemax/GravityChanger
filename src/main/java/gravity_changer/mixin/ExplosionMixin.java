package gravity_changer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.rotation.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// In 26.x Explosion is an interface and the logic is in ServerExplosion.
// The vanilla code already uses getEyePosition() (which is gravity-aware),
// so only the knockback needs to be converted from world space to entity space.
@Mixin(value = ServerExplosion.class, priority = 1001)
public abstract class ExplosionMixin {
    @WrapOperation(
        method = "hurtEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;pushFromExplosion(Lnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private void wrapOperation_hurtEntities_pushFromExplosion(Entity entity, Vec3 push, Operation<Void> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            original.call(entity, push);
            return;
        }

        original.call(entity, RotationUtil.vecWorldToPlayer(push, gravityDirection));
    }
}
