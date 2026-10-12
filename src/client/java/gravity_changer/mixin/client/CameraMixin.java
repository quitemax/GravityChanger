package gravity_changer.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.rotation.RotationAnimationComponent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Camera.class, priority = 1001)
public abstract class CameraMixin {
    @Shadow
    private Entity entity;

    @Shadow
    @Final
    private Quaternionf rotation;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    private float eyeHeight;

    @WrapOperation(
        method = "alignWithEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
            ordinal = 0
        )
    )
    private void wrapOperation_alignWithEntity_setPosition(
        Camera camera, double x, double y, double z,
        Operation<Void> original, float partialTick
    ) {
        Entity focusedEntity = this.entity;
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(focusedEntity);
        RotationAnimationComponent animation = GravityChangerAPI.getRotationAnimation(focusedEntity);

        if (animation == null) {
            original.call(camera, x, y, z);
            return;
        }

        long timeMs = focusedEntity.level().getGameTime() * 50 + (long) (partialTick * 50);
        animation.update(timeMs);
        if (gravityDirection == Direction.DOWN && !animation.isInAnimation()) {
            original.call(camera, x, y, z);
            return;
        }

        Quaternionf gravityRotation = animation.getCurrentGravityRotation(gravityDirection, timeMs);

        double entityX = net.minecraft.util.Mth.lerp((double) partialTick, focusedEntity.xo, focusedEntity.getX());
        double entityY = net.minecraft.util.Mth.lerp((double) partialTick, focusedEntity.yo, focusedEntity.getY());
        double entityZ = net.minecraft.util.Mth.lerp((double) partialTick, focusedEntity.zo, focusedEntity.getZ());

        double currentCameraY = net.minecraft.util.Mth.lerp(partialTick, this.eyeHeightOld, this.eyeHeight);

        Vec3 eyeOffset = animation.getEyeOffset(
            gravityRotation,
            new Vec3(0, currentCameraY, 0),
            gravityDirection
        );

        original.call(
            camera,
            entityX + eyeOffset.x(),
            entityY + eyeOffset.y(),
            entityZ + eyeOffset.z()
        );
    }

    @Inject(
        method = "setRotation(FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;",
            shift = At.Shift.AFTER,
            remap = false
        )
    )
    private void inject_setRotation(float yRot, float xRot, CallbackInfo ci) {
        if (this.entity == null) {
            return;
        }
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.entity);
        RotationAnimationComponent animation = GravityChangerAPI.getRotationAnimation(this.entity);
        if (animation == null) {
            return;
        }
        if (gravityDirection == Direction.DOWN && !animation.isInAnimation()) {
            return;
        }
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        long timeMs = this.entity.level().getGameTime() * 50 + (long) (partialTick * 50);
        Quaternionf gravityRotation = new Quaternionf(animation.getCurrentGravityRotation(gravityDirection, timeMs));
        gravityRotation.conjugate();
        gravityRotation.mul(this.rotation);
        this.rotation.set(gravityRotation.x(), gravityRotation.y(), gravityRotation.z(), gravityRotation.w());
    }
}
