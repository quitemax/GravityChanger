package gravity_changer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.rotation.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@SuppressWarnings("resource")
@Mixin(value = Player.class, priority = 1001)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow
    @Final
    private Abilities abilities;
    
//    @Shadow
//    public abstract EntityDimensions getDimensions(Pose pose);
    
    @Shadow
    protected abstract boolean isStayingOnGroundSurface();
    
    @Shadow
    protected abstract boolean isAboveGround(float maxDownStep);
    
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }
    
    @WrapOperation(
        method = "travel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getLookAngle()Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 wrapOperation_travel_getRotationVector_0(Player playerEntity, Operation<Vec3> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(playerEntity);
        if (gravityDirection == Direction.DOWN) {
            return original.call(playerEntity);
        }
        
        return RotationUtil.vecWorldToPlayer(original.call(playerEntity), gravityDirection);
    }
    
    
    @ModifyArgs(
        method = "travel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"
        )
    )
    private void modify_move_multiply_0(Args args) {
        Vec3 rotate = new Vec3(0.0D, 1.0D - 0.1D, 0.0D);
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection(this));
        args.set(0, (double) args.get(0) - rotate.x);
        args.set(1, (double) args.get(1) - rotate.y + (1.0D - 0.1D));
        args.set(2, (double) args.get(2) - rotate.z);
    }
    //@Redirect(
    //        method = "travel",
    //        at = @At(
    //                value = "NEW",
    //                target = "Lnet/minecraft/util/math/BlockPos;<init>(DDD)V",
    //                ordinal = 0
    //        )
    //)
    //private BlockPos redirect_travel_new_0(double x, double y, double z) {
    //    Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity)(Object)this);
    //    if(gravityDirection == Direction.DOWN) {
    //        return new BlockPos(x, y, z);
    //    }
//
    //    return new BlockPos(this.getPos().add(RotationUtil.vecPlayerToWorld(0.0D, 1.0D - 0.1D, 0.0D, gravityDirection)));
    //}
    

    

    
    @Inject(
        method = "Lnet/minecraft/world/entity/player/Player;maybeBackOffFromEdge(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)Lnet/minecraft/world/phys/Vec3;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_adjustMovementForSneaking(Vec3 movement, MoverType type, CallbackInfoReturnable<Vec3> cir) {
        Entity this_ = (Entity) (Object) this;
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this_);
        if (gravityDirection == Direction.DOWN) return;
        
        Vec3 playerMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        
        if (!this.abilities.flying && (type == MoverType.SELF || type == MoverType.PLAYER) && this.isStayingOnGroundSurface() && this.isAboveGround(this.maxUpStep())) {
            double d = playerMovement.x;
            double e = playerMovement.z;
            double var7 = 0.05D;
            
            while (d != 0.0D && this_.level().noCollision(this, this.getBoundingBox().move(RotationUtil.vecPlayerToWorld(d, (double) (-this.maxUpStep()), 0.0D, gravityDirection)))) {
                if (d < 0.05D && d >= -0.05D) {
                    d = 0.0D;
                }
                else if (d > 0.0D) {
                    d -= 0.05D;
                }
                else {
                    d += 0.05D;
                }
            }
            
            while (e != 0.0D && this_.level().noCollision(this, this.getBoundingBox().move(RotationUtil.vecPlayerToWorld(0.0D, (double) (-this.maxUpStep()), e, gravityDirection)))) {
                if (e < 0.05D && e >= -0.05D) {
                    e = 0.0D;
                }
                else if (e > 0.0D) {
                    e -= 0.05D;
                }
                else {
                    e += 0.05D;
                }
            }
            
            while (d != 0.0D && e != 0.0D && this_.level().noCollision(this, this.getBoundingBox().move(RotationUtil.vecPlayerToWorld(d, (double) (-this.maxUpStep()), e, gravityDirection)))) {
                if (d < 0.05D && d >= -0.05D) {
                    d = 0.0D;
                }
                else if (d > 0.0D) {
                    d -= 0.05D;
                }
                else {
                    d += 0.05D;
                }
                
                if (e < 0.05D && e >= -0.05D) {
                    e = 0.0D;
                }
                else if (e > 0.0D) {
                    e -= 0.05D;
                }
                else {
                    e += 0.05D;
                }
            }
            
            cir.setReturnValue(RotationUtil.vecPlayerToWorld(d, playerMovement.y, e, gravityDirection));
        }
        else {
            cir.setReturnValue(movement);
        }
    }
    

    

    

    

    

    

    
    @ModifyArgs(
        method = "aiStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"
        )
    )
    private void modify_tickMovement_expand_0(Args args) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        Vec3 vec3d = RotationUtil.maskPlayerToWorld(args.get(0), args.get(1), args.get(2), gravityDirection);
        args.set(0, vec3d.x);
        args.set(1, vec3d.y);
        args.set(2, vec3d.z);
    }
    
    @WrapOperation(
        method = "canPlayerFitWithinBlocksAndEntitiesWhen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EntityDimensions;makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;"
        )
    )
    private AABB wrapOperation_canPlayerFitWithinBlocksAndEntitiesWhen_getBoundingBox(
        EntityDimensions dimensions, Vec3 pos, Operation<AABB> original
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return original.call(dimensions, pos);
        }
        
        AABB result = RotationUtil.makeBoxFromDimensions(dimensions, gravityDirection, pos);
        return result;
    }

    // In 26.x isAboveGround() uses canFallAtLeast(), which builds the checked box in world space.
    // Build it in the entity's local frame instead.
    @Inject(
        method = "canFallAtLeast",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_canFallAtLeast(double deltaX, double deltaZ, double distance, CallbackInfoReturnable<Boolean> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;

        AABB local = RotationUtil.boxWorldToPlayer(this.getBoundingBox(), gravityDirection);
        AABB checkBox = new AABB(
            local.minX + 1.0E-7 + deltaX, local.minY - distance - 1.0E-7, local.minZ + 1.0E-7 + deltaZ,
            local.maxX - 1.0E-7 + deltaX, local.minY, local.maxZ - 1.0E-7 + deltaZ
        );
        cir.setReturnValue(this.level().noCollision(this, RotationUtil.boxPlayerToWorld(checkBox, gravityDirection)));
    }
}
