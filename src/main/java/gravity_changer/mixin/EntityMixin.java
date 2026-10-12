package gravity_changer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.objectweb.asm.Opcodes;
import gravity_changer.GravityChangerMod;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.rotation.RotationUtil;
import gravity_changer.util.GravityCollision;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;
import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private Vec3 position;
    
    @Shadow
    private EntityDimensions dimensions;
    
    @Shadow
    private float eyeHeight;
    
    @Shadow
    public double xo;
    
    @Shadow
    public double yo;
    
    @Shadow
    public double zo;
    
    @Shadow
    public abstract double getX();
    
    @Shadow
    public abstract Vec3 getEyePosition();
    
    @Shadow
    public abstract double getY();
    
    @Shadow
    public abstract double getZ();
    
    @Shadow
    public Level level;
    
    @Shadow
    public abstract int getBlockX();
    
    @Shadow
    public abstract int getBlockZ();
    
    @Shadow
    public boolean noPhysics;
    
    @Shadow
    public abstract Vec3 getDeltaMovement();
    
    @Shadow
    public abstract boolean isVehicle();
    
    @Shadow
    public abstract AABB getBoundingBox();
    
    @Shadow
    public static Vec3 collideWithShapes(Vec3 movement, AABB entityBoundingBox, List<VoxelShape> collisions) {
        return null;
    }
    
    @Shadow
    public abstract Vec3 position();
    
    
    @Shadow
    public abstract boolean isPassengerOfSameVehicle(Entity entity);
    
    @Shadow
    public abstract void push(double deltaX, double deltaY, double deltaZ);
    
    @Shadow
    protected abstract void onBelowWorld();
    
    @Shadow
    public abstract double getEyeY();
    
    @Shadow
    public abstract float getViewYRot(float tickDelta);
    
    @Shadow
    public abstract float getYRot();
    
    @Shadow
    public abstract float getXRot();
    
    @Shadow
    @Final
    protected RandomSource random;
    
    @Shadow
    public double fallDistance;

    @Shadow
    public boolean verticalCollision;

    @Shadow
    public boolean horizontalCollision;

    @Shadow
    public Optional<BlockPos> mainSupportingBlockPos;

    @Shadow
    private boolean onGroundNoBlocks;
    
    @SuppressWarnings("ConstantValue")
    @Inject(
        method = "Lnet/minecraft/world/entity/Entity;makeBoundingBox()Lnet/minecraft/world/phys/AABB;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void inject_calculateBoundingBox(CallbackInfoReturnable<AABB> cir) {
        Entity entity = ((Entity) (Object) this);
        if (entity instanceof Projectile) return;
        
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        AABB box = cir.getReturnValue().move(this.position.reverse());
        if (gravityDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            box = box.move(0.0D, -1.0E-6D, 0.0D);
        }
        cir.setReturnValue(RotationUtil.boxPlayerToWorld(box, gravityDirection).move(this.position));
    }
    
    // In 26.x calculateViewVector(float, float) is static, so hook its instance users instead.
    @Inject(
        method = {
            "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;",
            "Lnet/minecraft/world/entity/Entity;getUpVector(F)Lnet/minecraft/world/phys/Vec3;"
        },
        at = @At("RETURN"),
        cancellable = true
    )
    private void inject_getRotationVector(float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        cir.setReturnValue(RotationUtil.vecPlayerToWorld(cir.getReturnValue(), gravityDirection));
    }

    @Inject(
        method = "Lnet/minecraft/world/entity/Entity;getLookAngle()Lnet/minecraft/world/phys/Vec3;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void inject_getLookAngle(CallbackInfoReturnable<Vec3> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        cir.setReturnValue(RotationUtil.vecPlayerToWorld(cir.getReturnValue(), gravityDirection));
    }
    
    @Inject(
        method = "Lnet/minecraft/world/entity/Entity;getBlockPosBelowThatAffectsMyMovement()Lnet/minecraft/core/BlockPos;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_getVelocityAffectingPos(CallbackInfoReturnable<BlockPos> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        cir.setReturnValue(BlockPos.containing(this.position.add(Vec3.atLowerCornerOf(gravityDirection.getUnitVec3i()).scale(0.5000001D))));
    }
    
    @Inject(
        method = "Lnet/minecraft/world/entity/Entity;getEyePosition()Lnet/minecraft/world/phys/Vec3;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_getEyePos(CallbackInfoReturnable<Vec3> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        cir.setReturnValue(RotationUtil.vecPlayerToWorld(0.0D, this.eyeHeight, 0.0D, gravityDirection).add(this.position));
    }
    
    @Inject(
        method = "Lnet/minecraft/world/entity/Entity;getEyePosition(F)Lnet/minecraft/world/phys/Vec3;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_getCameraPosVec(float tickDelta, CallbackInfoReturnable<Vec3> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        Vec3 vec3d = RotationUtil.vecPlayerToWorld(0.0D, this.eyeHeight, 0.0D, gravityDirection);
        
        double d = Mth.lerp((double) tickDelta, this.xo, this.getX()) + vec3d.x;
        double e = Mth.lerp((double) tickDelta, this.yo, this.getY()) + vec3d.y;
        double f = Mth.lerp((double) tickDelta, this.zo, this.getZ()) + vec3d.z;
        cir.setReturnValue(new Vec3(d, e, f));
    }
    
    @Inject(
        method = "Lnet/minecraft/world/entity/Entity;getLightLevelDependentMagicValue()F",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_getBrightnessAtFEyes(CallbackInfoReturnable<Float> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        cir.setReturnValue(this.level.hasChunkAt(this.getBlockX(), this.getBlockZ()) ? this.level.getLightLevelDependentMagicValue(BlockPos.containing(this.getEyePosition())) : 0.0F);
    }
    
    // transform move vector from local to world (the velocity is local)

    
    // looks like not useful
//    @ModifyArg(
//        method = "move",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/world/phys/Vec3;multiply(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
//            ordinal = 0
//        ),
//        index = 0
//    )
//    private Vec3 modify_move_multiply_0(Vec3 vec3d) {
//        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
//        if (gravityDirection == Direction.DOWN) {
//            return vec3d;
//        }
//
//        return RotationUtil.maskPlayerToWorld(vec3d, gravityDirection);
//    }
    
    // transform the argument vector back to local coordinate

    
    // transform the local variable (result from collide()) to local coordinate

    
    @Inject(
        method = "Lnet/minecraft/world/entity/Entity;getOnPosLegacy()Lnet/minecraft/core/BlockPos;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_getLandingPos(CallbackInfoReturnable<BlockPos> cir) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        BlockPos blockPos = BlockPos.containing(RotationUtil.vecPlayerToWorld(0.0D, -0.20000000298023224D, 0.0D, gravityDirection).add(this.position));
        cir.setReturnValue(blockPos);
    }
    
    // transform the argument to local coordinate

    
    // transform the result to world coordinate
    // the input to Entity.collideBoundingBox will be in local coord

    
    // the argument was transformed to local coord,
    // but bounding box stretch needs world coord

    
    // the argument was transformed to local coord,
    // but bounding box move needs world coord

    
    // Entity.collideBoundingBox is inputed with local coord, transform it to world coord

    
    // transform back to local coord

    

    
    @ModifyArgs(
        method = "isInWall",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;ofSize(Lnet/minecraft/world/phys/Vec3;DDD)Lnet/minecraft/world/phys/AABB;",
            ordinal = 0
        )
    )
    private void modify_isInsideWall_of_0(Args args) {
        Vec3 rotate = new Vec3(args.get(1), args.get(2), args.get(3));
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        args.set(1, rotate.x);
        args.set(2, rotate.y);
        args.set(3, rotate.z);
    }
    
    @ModifyArg(
        method = "getDirection",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/Direction;fromYRot(D)Lnet/minecraft/core/Direction;"
        )
    )
    private double redirect_getHorizontalFacing_getYaw_0(double rotation) {
        Entity this_ = (Entity) (Object) this;
        
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this_);
        if (gravityDirection == Direction.DOWN) {
            return rotation;
        }
        
        return RotationUtil.rotPlayerToWorld((float) rotation, this.getXRot(), gravityDirection).x;
    }
    
    @Inject(
        method = "Lnet/minecraft/world/entity/Entity;spawnSprintParticle()V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_spawnSprintingParticles(CallbackInfo ci) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) return;
        
        ci.cancel();
        
        Vec3 floorPos = this.position().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.20000000298023224D, 0.0D, gravityDirection));
        
        BlockPos blockPos = BlockPos.containing(floorPos);
        BlockState blockState = this.level.getBlockState(blockPos);
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            Vec3 particlePos = this.position().add(
                RotationUtil.vecPlayerToWorld(
                        (this.random.nextDouble() - 0.5D) * (double) this.dimensions.width(),
                        0.1D,
                        (this.random.nextDouble() - 0.5D) * (double) this.dimensions.width(),
                        gravityDirection
                ));
            Vec3 playerVelocity = this.getDeltaMovement();
            Vec3 particleVelocity = RotationUtil.vecPlayerToWorld(
                playerVelocity.x * -4.0D,
                1.5D,
                playerVelocity.z * -4.0D,
                gravityDirection
            );
            this.level.addParticle(
                new BlockParticleOption(
                    ParticleTypes.BLOCK,
                    blockState
                ),
                particlePos.x,
                particlePos.y,
                particlePos.z,
                particleVelocity.x,
                particleVelocity.y,
                particleVelocity.z
            );
        }
    }
    
    
    
    
    @Inject(
        method = "Lnet/minecraft/world/entity/Entity;push(Lnet/minecraft/world/entity/Entity;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_pushAwayFrom(Entity entity, CallbackInfo ci) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        Direction otherGravityDirection = GravityChangerAPI.getGravityDirection(entity);
        
        if (gravityDirection == Direction.DOWN && otherGravityDirection == Direction.DOWN) return;
        
        ci.cancel();
        
        if (!this.isPassengerOfSameVehicle(entity)) {
            if (!entity.noPhysics && !this.noPhysics) {
                Vec3 entityOffset = entity.getBoundingBox().getCenter().subtract(this.getBoundingBox().getCenter());
                
                {
                    Vec3 playerEntityOffset = RotationUtil.vecWorldToPlayer(entityOffset, gravityDirection);
                    double dx = playerEntityOffset.x;
                    double dz = playerEntityOffset.z;
                    double f = Mth.absMax(dx, dz);
                    if (f >= 0.009999999776482582D) {
                        f = Math.sqrt(f);
                        dx /= f;
                        dz /= f;
                        double g = 1.0D / f;
                        if (g > 1.0D) {
                            g = 1.0D;
                        }
                        
                        dx *= g;
                        dz *= g;
                        dx *= 0.05000000074505806D;
                        dz *= 0.05000000074505806D;
                        if (!this.isVehicle()) {
                            this.push(-dx, 0.0D, -dz);
                        }
                    }
                }
                
                {
                    Vec3 entityEntityOffset = RotationUtil.vecWorldToPlayer(entityOffset, otherGravityDirection);
                    double dx = entityEntityOffset.x;
                    double dz = entityEntityOffset.z;
                    double f = Mth.absMax(dx, dz);
                    if (f >= 0.009999999776482582D) {
                        f = Math.sqrt(f);
                        dx /= f;
                        dz /= f;
                        double g = 1.0D / f;
                        if (g > 1.0D) {
                            g = 1.0D;
                        }
                        
                        dx *= g;
                        dz *= g;
                        dx *= 0.05000000074505806D;
                        dz *= 0.05000000074505806D;
                        if (!entity.isVehicle()) {
                            entity.push(dx, 0.0D, dz);
                        }
                    }
                }
            }
        }
    }
    
    @Inject(
        method = "Lnet/minecraft/world/entity/Entity;checkBelowWorld()V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_attemptTickInVoid(CallbackInfo ci) {
        Entity this_ = (Entity) (Object) this;
    
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this_);
        if (GravityChangerMod.config.voidDamageAboveWorld &&
            this.getY() > (double) (this.level.getMaxY() + 256) &&
            gravityDirection == Direction.UP
        ) {
            this.onBelowWorld();
            ci.cancel();
            return;
        }
        
        if (GravityChangerMod.config.voidDamageOnHorizontalFallTooFar &&
            gravityDirection.getAxis() != Direction.Axis.Y &&
            fallDistance > 1024
            // TODO also handle reverse gravity strength
        ) {
            this.onBelowWorld();
            ci.cancel();
            return;
        }
    }
    
    @ModifyArgs(
        method = "isFree(DDD)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;",
            ordinal = 0
        )
    )
    private void redirect_doesNotCollide_offset_0(Args args) {
        Vec3 rotate = new Vec3(args.get(0), args.get(1), args.get(2));
        rotate = RotationUtil.vecPlayerToWorld(rotate, GravityChangerAPI.getGravityDirection((Entity) (Object) this));
        args.set(0, rotate.x);
        args.set(1, rotate.y);
        args.set(2, rotate.z);
    }

    // In 26.x all gravity goes through Entity.getGravity() and Entity.applyGravity()
    // (and LivingEntity.getEffectiveGravity() which uses getGravity()).
    @ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
    private double gravitychanger$multiplyGravity(double original) {
        return original * GravityChangerAPI.getGravityStrength((Entity) (Object) this);
    }

    @WrapOperation(
        method = "applyGravity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$applyGravityInDirection(Vec3 velocity, double x, double y, double z, Operation<Vec3> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return original.call(velocity, x, y, z);
        }
        Vec3 worldDelta = RotationUtil.vecPlayerToWorld(x, y, z, gravityDirection);
        return original.call(velocity, worldDelta.x, worldDelta.y, worldDelta.z);
    }

    // walking input is computed from the local yaw; velocity is in world space
    @WrapOperation(
        method = "moveRelative",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$inputVectorToWorld(Vec3 input, float speed, float yRot, Operation<Vec3> original) {
        Vec3 result = original.call(input, speed, yRot);
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return result;
        }
        return RotationUtil.vecPlayerToWorld(result, gravityDirection);
    }

    // Entity.collide: step-up and axis ordering relative to the gravity direction
    @Inject(
        method = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void gravitychanger$collide(Vec3 delta, CallbackInfoReturnable<Vec3> cir) {
        Entity self = (Entity) (Object) this;
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) return;

        cir.setReturnValue(GravityCollision.collide(self, delta, gravityDirection));
    }

    @WrapOperation(
        method = "Lnet/minecraft/world/entity/Entity;collideBoundingBox(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;collideWithShapes(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private static Vec3 gravitychanger$collideWithShapes(
        Vec3 movement, AABB box, List<VoxelShape> shapes, Operation<Vec3> original,
        @Local(argsOnly = true) Entity entity
    ) {
        if (entity == null) return original.call(movement, box, shapes);
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) return original.call(movement, box, shapes);
        return GravityCollision.collideWithShapes(movement, box, shapes, gravityDirection);
    }

    // After a collision vanilla zeroes/bounces world x, z (horizontal) and world y (vertical).
    // Do it in the gravity-local frame instead, otherwise walking along world Y gets damped
    // every tick while standing on a wall.
    @WrapOperation(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;restituteMovementAfterCollisions(Lnet/minecraft/world/level/block/state/BlockState;ZZLnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private void gravitychanger$restituteInLocalFrame(
        Entity self, BlockState state, boolean xCollision, boolean zCollision, Vec3 movement, Operation<Void> original,
        @Local(index = 2) Vec3 delta
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) {
            original.call(self, state, xCollision, zCollision, movement);
            return;
        }

        Vec3 localDelta = RotationUtil.vecWorldToPlayer(delta, gravityDirection);
        Vec3 localMovement = RotationUtil.vecWorldToPlayer(movement, gravityDirection);
        boolean localX = !Mth.equal(localDelta.x, localMovement.x);
        boolean localZ = !Mth.equal(localDelta.z, localMovement.z);

        self.setDeltaMovement(RotationUtil.vecWorldToPlayer(self.getDeltaMovement(), gravityDirection));
        original.call(self, state, localX, localZ, localMovement);
        self.setDeltaMovement(RotationUtil.vecPlayerToWorld(self.getDeltaMovement(), gravityDirection));
    }

    // block speed factor (soul sand, honey) slows the horizontal plane, which is not world x/z
    @WrapOperation(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 gravitychanger$speedFactorInLocalFrame(
        Vec3 velocity, double x, double y, double z, Operation<Vec3> original
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return original.call(velocity, x, y, z);
        }
        Vec3 local = RotationUtil.vecWorldToPlayer(velocity, gravityDirection);
        return RotationUtil.vecPlayerToWorld(original.call(local, x, y, z), gravityDirection);
    }

    // move() passes the world-space movement.y to checkFallDamage; use the gravity-local component
    // so fallDistance accumulates for mobs on the server and for the local player on the client.
    @WrapOperation(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"
        )
    )
    private void gravitychanger$moveCheckFallDamage(
        Entity self, double y, boolean onGround, BlockState state, BlockPos pos, Operation<Void> original,
        @Local(index = 4) Vec3 movement
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) {
            original.call(self, y, onGround, state, pos);
            return;
        }
        original.call(self, RotationUtil.vecWorldToPlayer(movement, gravityDirection).y, onGround, state, pos);
    }

    // Vanilla decides "landed" by (collided && delta.y < 0) in world space. Redo the vertical/horizontal
    // collision flags relative to the gravity direction so onGround (jumping, friction, fall damage) works.
    @WrapOperation(
        method = "move",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;verticalCollisionBelow:Z",
            opcode = Opcodes.PUTFIELD,
            ordinal = 1
        )
    )
    private void gravitychanger$collisionFlagsByGravity(
        Entity self, boolean value, Operation<Void> original,
        @Local(index = 2) Vec3 delta, @Local(index = 4) Vec3 movement
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) {
            original.call(self, value);
            return;
        }

        Direction.Axis axis = gravityDirection.getAxis();
        double wanted = delta.get(axis);
        this.verticalCollision = wanted != movement.get(axis);

        boolean horizontal = false;
        for (Direction.Axis other : Direction.Axis.values()) {
            if (other != axis && !Mth.equal(delta.get(other), movement.get(other))) {
                horizontal = true;
            }
        }
        this.horizontalCollision = horizontal;

        int step = gravityDirection.getAxisDirection().getStep();
        original.call(self, this.verticalCollision && wanted * step > 0.0D);
    }

    // look for the supporting block on the gravity side of the bounding box instead of world-below
    @Inject(
        method = "checkSupportingBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    private void gravitychanger$checkSupportingBlock(boolean onGround, Vec3 movement, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(self);
        if (gravityDirection == Direction.DOWN) return;
        ci.cancel();

        if (!onGround) {
            this.onGroundNoBlocks = false;
            if (this.mainSupportingBlockPos.isPresent()) {
                this.mainSupportingBlockPos = Optional.empty();
            }
            return;
        }

        AABB box = this.getBoundingBox();
        double eps = 1.0E-6D;
        double minX = box.minX, minY = box.minY, minZ = box.minZ, maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;
        switch (gravityDirection) {
            case UP -> { minY = maxY; maxY += eps; }
            case DOWN -> { maxY = minY; minY -= eps; }
            case EAST -> { minX = maxX; maxX += eps; }
            case WEST -> { maxX = minX; minX -= eps; }
            case SOUTH -> { minZ = maxZ; maxZ += eps; }
            case NORTH -> { maxZ = minZ; minZ -= eps; }
        }
        AABB probe = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

        Optional<BlockPos> support = this.level.findSupportingBlock(self, probe);
        if (support.isPresent() || this.onGroundNoBlocks) {
            this.mainSupportingBlockPos = support;
        } else if (movement != null) {
            // undo the part of the move that is perpendicular to gravity and look again
            Vec3 along = Vec3.atLowerCornerOf(gravityDirection.getUnitVec3i());
            Vec3 back = movement.reverse();
            back = back.subtract(along.scale(back.dot(along)));
            support = this.level.findSupportingBlock(self, probe.move(back));
            this.mainSupportingBlockPos = support;
        }
        this.onGroundNoBlocks = support.isEmpty();
    }

    // make sure fall distance is correct on the server side of the player
    @ModifyArgs(
        method = "doCheckFallDamage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"
        )
    )
    private void gravitychanger$wrapCheckFallDamage(Args args, double dx, double dy, double dz, boolean onGround) {
        if (!((Object) this instanceof net.minecraft.server.level.ServerPlayer)) return;
        Direction gravity = GravityChangerAPI.getGravityDirection((Entity) (Object) this);
        if (gravity == Direction.DOWN) return;

        Vec3 localVec = RotationUtil.vecWorldToPlayer(dx, dy, dz, gravity);
        args.set(0, localVec.y());
    }
}
