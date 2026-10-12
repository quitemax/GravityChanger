package gravity_changer.mixin;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.rotation.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract EntityDimensions getDimensions(Pose pose);
    
    @Shadow
    public abstract float getViewYRot(float tickDelta);
    
    
    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }
    

    

    

    

    

    
    @ModifyArg(
        method = "playBlockFallSound",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
        ),
        index = 0
    )
    private BlockPos modify_playBlockFallSound_getBlockState_0(BlockPos blockPos) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return blockPos;
        }
        
        return BlockPos.containing(this.position().add(RotationUtil.vecPlayerToWorld(0, -0.20000000298023224D, 0, gravityDirection)));
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;hasLineOfSight(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/ClipContext$Block;Lnet/minecraft/world/level/ClipContext$Fluid;D)Z",
        at = @At(
            value = "NEW",
            target = "(DDD)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        )
    )
    private Vec3 redirect_canSee_new_0(double x, double y, double z) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return new Vec3(x, y, z);
        }
        
        return this.getEyePosition();
    }
    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;hasLineOfSight(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/ClipContext$Block;Lnet/minecraft/world/level/ClipContext$Fluid;D)Z",
        at = @At(
            value = "NEW",
            target = "(DDD)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 1
        )
    )
    private Vec3 redirect_canSee_new_1(double x, double y, double z, Entity entity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return new Vec3(x, y, z);
        }
        
        return entity.getEyePosition();
    }
    
    @Inject(
        method = "Lnet/minecraft/world/entity/LivingEntity;getLocalBoundsForPose(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/phys/AABB;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void inject_getBoundingBox(Pose pose, CallbackInfoReturnable<AABB> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        AABB box = cir.getReturnValue();
        if (gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            box = box.move(0.0D, -1.0E-6D, 0.0D);
        }
        cir.setReturnValue(RotationUtil.boxPlayerToWorld(box, gravityDirection));
    }

//    @Inject(
//            method = "updateLimbs",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void inject_updateLimbs(LivingEntity entity, boolean flutter, CallbackInfo ci) {
//        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
//        if(gravityDirection == Direction.DOWN) return;
//
//        ci.cancel();
//
//        Vec3d playerPosDelta = RotationUtil.vecWorldToPlayer(entity.getX() - entity.prevX, entity.getY() - entity.prevY, entity.getZ() - entity.prevZ, gravityDirection);
//
//        entity.lastLimbDistance = entity.limbDistance;
//        double d = playerPosDelta.x;
//        double e = flutter ? playerPosDelta.y : 0.0D;
//        double f = playerPosDelta.z;
//        float g = (float)Math.sqrt(d * d + e * e + f * f) * 4.0F;
//        if (g > 1.0F) {
//            g = 1.0F;
//        }
//
//        entity.limbDistance += (g - entity.limbDistance) * 0.4F;
//        entity.limbAngle += entity.limbDistance;
//    }
    
    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getX()D",
            ordinal = 0
        )
    )
    private double wrapOperation_tick_getX_0(LivingEntity livingEntity, Operation<Double> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return original.call(livingEntity);
        }
        
        return RotationUtil.vecWorldToPlayer(original.call(livingEntity) - livingEntity.xo, livingEntity.getY() - livingEntity.yo, livingEntity.getZ() - livingEntity.zo, gravityDirection).x + livingEntity.xo;
    }
    
    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D",
            ordinal = 0
        )
    )
    private double wrapOperation_tick_getZ_0(LivingEntity livingEntity, Operation<Double> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return original.call(livingEntity);
        }
        
        return RotationUtil.vecWorldToPlayer(livingEntity.getX() - livingEntity.xo, livingEntity.getY() - livingEntity.yo, original.call(livingEntity) - livingEntity.zo, gravityDirection).z + livingEntity.zo;
    }
    

    

    

    

    

    
    

    

    

    
    @Redirect(
        method = "Lnet/minecraft/world/entity/LivingEntity;baseTick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;",
            ordinal = 0
        )
    )
    private BlockPos redirect_baseTick_new_0(double x, double y, double z) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return BlockPos.containing(x, y, z);
        }
        
        return BlockPos.containing(this.getEyePosition());
    }
    
    @WrapOperation(
        method = "spawnItemParticles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        )
    )
    private Vec3 wrapOperation_spawnItemParticles_add_0(Vec3 vec3d, double x, double y, double z, Operation<Vec3> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return original.call(vec3d, x, y, z);
        }
        
        Vec3 rotated = RotationUtil.vecPlayerToWorld(vec3d, gravityDirection);
        return original.call(this.getEyePosition(), rotated.x, rotated.y, rotated.z);
    }
    
    @ModifyVariable(
        method = "Lnet/minecraft/world/entity/LivingEntity;spawnItemParticles(Lnet/minecraft/world/item/ItemStack;I)V",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/phys/Vec3;yRot(F)Lnet/minecraft/world/phys/Vec3;",
            ordinal = 0
        ),
        ordinal = 0
    )
    private Vec3 modify_spawnItemParticles_Vec3d_0(Vec3 vec3d) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }
        
        return RotationUtil.vecPlayerToWorld(vec3d, gravityDirection);
    }
    
    @ModifyArgs(
        method = "tickEffects",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
        )
    )
    private void modify_tickStatusEffects_addParticle_0(Args args) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(args.get(1), args.get(2), args.get(3)), gravityDirection));
        args.set(1, vec3d.x);
        args.set(2, vec3d.y);
        args.set(3, vec3d.z);
    }
    
    @ModifyArgs(
        method = "makePoofParticles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
            ordinal = 0
        )
    )
    private void modify_addDeathParticless_addParticle_0(Args args) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        Vec3 vec3d = this.position().subtract(RotationUtil.vecPlayerToWorld(this.position().subtract(args.get(1), args.get(2), args.get(3)), gravityDirection));
        args.set(1, vec3d.x);
        args.set(2, vec3d.y);
        args.set(3, vec3d.z);
    }
    

    
    // TODO shield knockback
//    @ModifyArg(
//        method = "blockedByShield",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/world/phys/Vec3;vectorTo(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
//            ordinal = 0
//        ),
//        index = 0
//    )
//    private Vec3 modify_blockedByShield_relativize_0(Vec3 vec3d) {
//        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity)(Object)this);
//        if(gravityDirection == Direction.DOWN) {
//            return vec3d;
//        }
//
//        return this.getEyePosition();
//    }

//    @ModifyVariable(
//        method = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z",
//        at = @At(
//            value = "INVOKE_ASSIGN",
//            target = "Lnet/minecraft/world/phys/Vec3;normalize()Lnet/minecraft/world/phys/Vec3;",
//            ordinal = 0
//        ),
//        ordinal = 2
//    )
//    private Vec3 modify_blockedByShield_Vec3d_2(Vec3 vec3d) {
//        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity)(Object)this);
//        if(gravityDirection == Direction.DOWN) {
//            return vec3d;
//        }
//
//        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
//    }
    

    
    @ModifyVariable(method = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(DF)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double diminishFallDamage(double value) {
        return value * Math.sqrt(GravityChangerAPI.getGravityStrength(this));
    }

    // ---- vertical position used by fluid movement (travelInAir / travelInFluid / jumpOutOfFluid)
    @Redirect(method = "travelInAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getY()D", ordinal = 0))
    private double redirect_travelInAir_getY(LivingEntity livingEntity) {
        return gravitychanger$localY(livingEntity);
    }

    @Redirect(method = "travelInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getY()D", ordinal = 0))
    private double redirect_travelInFluid_getY(LivingEntity livingEntity) {
        return gravitychanger$localY(livingEntity);
    }

    @Redirect(method = "jumpOutOfFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getY()D", ordinal = 0))
    private double redirect_jumpOutOfFluid_getY(LivingEntity livingEntity) {
        return gravitychanger$localY(livingEntity);
    }

    private static double gravitychanger$localY(LivingEntity livingEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return livingEntity.getY();
        }
        return RotationUtil.vecWorldToPlayer(livingEntity.position(), gravityDirection).y;
    }

    // ---- Velocity (deltaMovement) is stored in world space, but vanilla walking/jumping/drag/gravity
    // code assumes "y is up". Convert to the local frame around those spots.
    @Shadow
    protected abstract float getJumpPower();

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void inject_jumpFromGround(CallbackInfo ci) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        ci.cancel();

        float jumpPower = this.getJumpPower();
        if (jumpPower <= 1.0E-5F) return;

        Vec3 local = RotationUtil.vecWorldToPlayer(this.getDeltaMovement(), gravityDirection);
        Vec3 jumped = new Vec3(local.x, Math.max(jumpPower, local.y), local.z);
        if (this.isSprinting()) {
            float yaw = this.getYRot() * 0.017453292F;
            jumped = jumped.add(-Mth.sin(yaw) * 0.2, 0.0, Mth.cos(yaw) * 0.2);
        }
        this.setDeltaMovement(RotationUtil.vecPlayerToWorld(jumped, gravityDirection));
        this.needsSync = true;
    }

    // travelInAir: work on the post-move velocity in the local frame (gravity and drag are applied there),
    // then write it back in world space.
    @WrapOperation(
        method = "travelInAir",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 wrapOperation_travelInAir_handleFriction(
        LivingEntity self, Vec3 input, float friction, Operation<Vec3> original
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(self);
        Vec3 result = original.call(self, input, friction);
        if (gravityDirection == Direction.DOWN) return result;
        return RotationUtil.vecWorldToPlayer(result, gravityDirection);
    }

    @WrapOperation(
        method = "travelInAir",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(DDD)V")
    )
    private void wrapOperation_travelInAir_setDeltaMovement(
        LivingEntity self, double x, double y, double z, Operation<Void> original
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) {
            original.call(self, x, y, z);
            return;
        }
        Vec3 world = RotationUtil.vecPlayerToWorld(x, y, z, gravityDirection);
        original.call(self, world.x, world.y, world.z);
    }

    @WrapOperation(
        method = "handleRelativeFrictionAndCalculateMovement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;handleOnClimbable(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 wrapOperation_handleOnClimbable(LivingEntity self, Vec3 velocity, Operation<Vec3> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) {
            return original.call(self, velocity);
        }
        Vec3 local = original.call(self, RotationUtil.vecWorldToPlayer(velocity, gravityDirection));
        return RotationUtil.vecPlayerToWorld(local, gravityDirection);
    }

    // ---- elytra: the look vector must be in the entity's local frame
    @WrapOperation(
        method = "updateFallFlyingMovement",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;")
    )
    private Vec3 wrapOperation_updateFallFlyingMovement_getLookAngle(LivingEntity livingEntity, Operation<Vec3> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return original.call(livingEntity);
        }
        return RotationUtil.vecWorldToPlayer(original.call(livingEntity), gravityDirection);
    }

    // ---- knockback direction (damage and shield blocking). Computed in this entity's local frame.
    @WrapOperation(
        method = "dealDefaultKnockback",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDDLnet/minecraft/world/damagesource/DamageSource;F)V"
        )
    )
    private void wrapOperation_dealDefaultKnockback_knockback(
        LivingEntity target, double power, double x, double z, DamageSource source, float f, Operation<Void> original
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        Vec3 sourcePos = source.getSourcePosition();
        Entity attacker = source.getDirectEntity();
        if (sourcePos == null || (gravityDirection == Direction.DOWN
            && (attacker == null || GravityChangerAPI.getGravityDirection(attacker) == Direction.DOWN))) {
            original.call(target, power, x, z, source, f);
            return;
        }
        if (attacker != null && GravityChangerAPI.getGravityDirection(attacker) != Direction.DOWN) {
            sourcePos = attacker.getEyePosition();
        }
        Vec3 rel = RotationUtil.vecWorldToPlayer(sourcePos.subtract(target.position()), gravityDirection);
        original.call(target, power, rel.x, rel.z, source, f);
    }

    @WrapOperation(
        method = "blockedByItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDDLnet/minecraft/world/damagesource/DamageSource;F)V"
        )
    )
    private void wrapOperation_blockedByItem_knockback(
        LivingEntity target, double power, double x, double z, DamageSource source, float f, Operation<Void> original,
        LivingEntity attacker
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN && GravityChangerAPI.getGravityDirection(attacker) == Direction.DOWN) {
            original.call(target, power, x, z, source, f);
            return;
        }
        Vec3 attackerPos = GravityChangerAPI.getGravityDirection(attacker) == Direction.DOWN ? attacker.position() : attacker.getEyePosition();
        Vec3 rel = RotationUtil.vecWorldToPlayer(attackerPos.subtract(target.position()), gravityDirection);
        original.call(target, power, rel.x, rel.z, source, f);
    }

    // ---- attacker yaw used for the knockback direction, converted into the target's local frame
    @WrapOperation(
        method = "causeExtraKnockback",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F", ordinal = 0)
    )
    private float wrapOperation_causeExtraKnockback_getYaw_0(LivingEntity attacker, Operation<Float> original, Entity target) {
        return gravitychanger$attackYaw(attacker, target, original);
    }

    @WrapOperation(
        method = "causeExtraKnockback",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F", ordinal = 1)
    )
    private float wrapOperation_causeExtraKnockback_getYaw_1(LivingEntity attacker, Operation<Float> original, Entity target) {
        return gravitychanger$attackYaw(attacker, target, original);
    }

    private static float gravitychanger$attackYaw(LivingEntity attacker, Entity target, Operation<Float> original) {
        Direction targetDir = GravityChangerAPI.getGravityDirection(target);
        Direction attackerDir = GravityChangerAPI.getGravityDirection(attacker);
        if (targetDir == attackerDir) {
            return original.call(attacker);
        }
        return RotationUtil.rotWorldToPlayer(
            RotationUtil.rotPlayerToWorld(original.call(attacker), attacker.getXRot(), attackerDir), targetDir
        ).x;
    }

    // ---- dropped items (player drop / throw)
    @Redirect(
        method = "createItemStackToDrop",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;",
            ordinal = 0
        )
    )
    private ItemEntity redirect_dropItem_new_0(
        Level world, double x, double y, double z, ItemStack stack
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return new ItemEntity(world, x, y, z, stack);
        }
        
        Vec3 vec3d = this.getEyePosition()
            .subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.3D, 0.0D, gravityDirection));
        
        ItemEntity itemEntity = new ItemEntity(world, vec3d.x, vec3d.y, vec3d.z, stack);
        
//        // change the gravity of the thrown item
//        GravityChangerAPI.setBaseGravityDirection(
//            itemEntity, gravityDirection
//        );
        // the item entity calculates position both on client and server separately
        // if gravity is not down, the client and server will desync (the reason is not yet known)
        // don't let item change gravity for now
        
        return itemEntity;
    }

    @WrapOperation(
        method = "createItemStackToDrop",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/ItemEntity;setDeltaMovement(DDD)V"
        )
    )
    private void wrapOperation_dropItem_setVelocity(ItemEntity itemEntity, double x, double y, double z, Operation<Void> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            original.call(itemEntity, x, y, z);
            return;
        }
        
        Vec3 world = RotationUtil.vecPlayerToWorld(x, y, z, gravityDirection);
        GravityChangerAPI.setWorldVelocity(itemEntity, world);
    }
}
