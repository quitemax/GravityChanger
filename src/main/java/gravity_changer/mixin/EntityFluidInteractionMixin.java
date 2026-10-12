package gravity_changer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.rotation.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// In 26.x the fluid logic moved from Entity into EntityFluidInteraction.
// It determines the "eye in fluid" state using getBlockX/getEyeY/getBlockZ,
// which are not gravity-aware, so use the eye position instead.
@Mixin(EntityFluidInteraction.class)
public abstract class EntityFluidInteractionMixin {
    @WrapOperation(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getBlockX()I",
            ordinal = 0
        )
    )
    private int wrap_update_getBlockX(Entity entity, Operation<Integer> original) {
        if (GravityChangerAPI.getGravityDirection(entity) == Direction.DOWN) {
            return original.call(entity);
        }
        return BlockPos.containing(entity.getEyePosition()).getX();
    }

    @WrapOperation(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getEyeY()D",
            ordinal = 0
        )
    )
    private double wrap_update_getEyeY(Entity entity, Operation<Double> original) {
        if (GravityChangerAPI.getGravityDirection(entity) == Direction.DOWN) {
            return original.call(entity);
        }
        return entity.getEyePosition().y();
    }

    @WrapOperation(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getBlockZ()I",
            ordinal = 0
        )
    )
    private int wrap_update_getBlockZ(Entity entity, Operation<Integer> original) {
        if (GravityChangerAPI.getGravityDirection(entity) == Direction.DOWN) {
            return original.call(entity);
        }
        return BlockPos.containing(entity.getEyePosition()).getZ();
    }
}
