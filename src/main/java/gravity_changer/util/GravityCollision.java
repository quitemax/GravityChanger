package gravity_changer.util;

import gravity_changer.rotation.RotationUtil;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Entity collision resolution relative to a (non-DOWN) gravity direction.
 * Mirrors vanilla Entity.collide / collideWithShapes but treats "up" as the opposite of gravity.
 */
public final class GravityCollision {
    private GravityCollision() {}

    /**
     * Vanilla collideWithShapes resolves world Y first, then the larger of X/Z.
     * Here the player-local Y goes first, then the larger of local X/Z. Works in world space.
     */
    public static Vec3 collideWithShapes(Vec3 movement, AABB box, List<VoxelShape> shapes, Direction gravity) {
        Vec3 local = RotationUtil.vecWorldToPlayer(movement, gravity);
        double lx = local.x;
        double ly = local.y;
        double lz = local.z;
        Direction dirX = RotationUtil.dirPlayerToWorld(Direction.EAST, gravity);
        Direction dirY = RotationUtil.dirPlayerToWorld(Direction.UP, gravity);
        Direction dirZ = RotationUtil.dirPlayerToWorld(Direction.SOUTH, gravity);
        int sx = dirX.getAxisDirection().getStep();
        int sy = dirY.getAxisDirection().getStep();
        int sz = dirZ.getAxisDirection().getStep();

        if (ly != 0.0D) {
            ly = Shapes.collide(dirY.getAxis(), box, shapes, ly * sy) * sy;
            if (ly != 0.0D) {
                box = box.move(RotationUtil.vecPlayerToWorld(0.0D, ly, 0.0D, gravity));
            }
        }

        boolean zFirst = Math.abs(lx) < Math.abs(lz);
        if (zFirst && lz != 0.0D) {
            lz = Shapes.collide(dirZ.getAxis(), box, shapes, lz * sz) * sz;
            if (lz != 0.0D) {
                box = box.move(RotationUtil.vecPlayerToWorld(0.0D, 0.0D, lz, gravity));
            }
        }

        if (lx != 0.0D) {
            lx = Shapes.collide(dirX.getAxis(), box, shapes, lx * sx) * sx;
            if (!zFirst && lx != 0.0D) {
                box = box.move(RotationUtil.vecPlayerToWorld(lx, 0.0D, 0.0D, gravity));
            }
        }

        if (!zFirst && lz != 0.0D) {
            lz = Shapes.collide(dirZ.getAxis(), box, shapes, lz * sz) * sz;
        }

        return RotationUtil.vecPlayerToWorld(lx, ly, lz, gravity);
    }

    /** Replacement for Entity.collide(Vec3), including step-up along the local "up" direction. */
    public static Vec3 collide(Entity entity, Vec3 delta, Direction gravity) {
        Level level = entity.level();
        AABB box = entity.getBoundingBox();
        Direction.Axis axis = gravity.getAxis();
        int gravityStep = gravity.getAxisDirection().getStep();
        int upStep = -gravityStep;
        float maxUpStep = entity.maxUpStep();

        Vec3 upVec = withAxis(Vec3.ZERO, axis, upStep * (double) maxUpStep);
        List<VoxelShape> entityColliders = level.getEntityCollisions(entity, box.expandTowards(delta).expandTowards(upVec));
        Vec3 movement = delta.lengthSqr() == 0.0D
            ? delta
            : Entity.collideBoundingBox(entity, delta, box, level, entityColliders);

        boolean gravityAxisCollided = delta.get(axis) != movement.get(axis);
        boolean hitGround = gravityAxisCollided && delta.get(axis) * gravityStep > 0.0D;
        boolean sideCollided = false;
        for (Direction.Axis other : Direction.Axis.values()) {
            if (other != axis && delta.get(other) != movement.get(other)) {
                sideCollided = true;
            }
        }

        if (maxUpStep > 0.0F && (hitGround || entity.onGround()) && sideCollided) {
            double baseShift = hitGround ? movement.get(axis) : 0.0D;
            AABB base = hitGround ? box.move(withAxis(Vec3.ZERO, axis, baseShift)) : box;

            AABB stepBox = base.expandTowards(withAxis(delta, axis, upStep * (double) maxUpStep));
            if (!hitGround) {
                stepBox = stepBox.expandTowards(withAxis(Vec3.ZERO, axis, gravityStep * 9.999999747378752E-6D));
            }
            List<VoxelShape> shapes = Entity.collectAllColliders(entity, level, stepBox);

            float localMovement = (float) (movement.get(axis) * upStep);
            for (float height : candidateStepUpHeights(base, shapes, maxUpStep, localMovement, axis, upStep)) {
                Vec3 stepDelta = withAxis(delta, axis, upStep * (double) height);
                Vec3 result = collideWithShapes(stepDelta, base, shapes, gravity);
                if (sideDistanceSqr(result, axis) > sideDistanceSqr(movement, axis)) {
                    return withAxis(result, axis, result.get(axis) + baseShift);
                }
            }
        }

        return movement;
    }

    private static float[] candidateStepUpHeights(
        AABB box, List<VoxelShape> shapes, float maxUpStep, float collisionY, Direction.Axis axis, int upStep
    ) {
        FloatArraySet heights = new FloatArraySet(4);
        for (VoxelShape shape : shapes) {
            DoubleList coords = shape.getCoords(axis);
            for (double coord : coords) {
                float height = (float) (upStep > 0 ? coord - box.min(axis) : box.max(axis) - coord);
                if (height < 0.0F || height == collisionY) continue;
                if (height > maxUpStep) continue;
                heights.add(height);
            }
        }
        float[] result = heights.toFloatArray();
        FloatArrays.unstableSort(result);
        return result;
    }

    private static double sideDistanceSqr(Vec3 v, Direction.Axis axis) {
        double sum = 0.0D;
        for (Direction.Axis other : Direction.Axis.values()) {
            if (other != axis) {
                double c = v.get(other);
                sum += c * c;
            }
        }
        return sum;
    }

    private static Vec3 withAxis(Vec3 v, Direction.Axis axis, double value) {
        return switch (axis) {
            case X -> new Vec3(value, v.y, v.z);
            case Y -> new Vec3(v.x, value, v.z);
            case Z -> new Vec3(v.x, v.y, value);
        };
    }
}
