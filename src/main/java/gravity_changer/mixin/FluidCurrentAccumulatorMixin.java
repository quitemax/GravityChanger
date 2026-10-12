package gravity_changer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.rotation.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// The fluid current is computed in world space, but the entity's velocity is in entity space.
@Mixin(targets = "net.minecraft.world.entity.EntityFluidInteraction$CurrentAccumulator")
public abstract class FluidCurrentAccumulatorMixin {
    @WrapOperation(
        method = "applyTo",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 wrap_applyTo_getDeltaMovement(Entity entity, Operation<Vec3> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return original.call(entity);
        }
        return RotationUtil.vecPlayerToWorld(original.call(entity), gravityDirection);
    }

    @WrapOperation(
        method = "applyTo",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;addDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private void wrap_applyTo_addDeltaMovement(Entity entity, Vec3 push, Operation<Void> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            original.call(entity, push);
            return;
        }
        original.call(entity, RotationUtil.vecWorldToPlayer(push, gravityDirection));
    }
}
