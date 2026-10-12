package gravity_changer.mixin.client;

import gravity_changer.api.GravityChangerAPI;
import gravity_changer.rotation.RotationUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
    // Vanilla samples the "is the camera inside a block" points around (getX, getEyeY, getZ) with
    // axis-aligned offsets, which for non-DOWN gravity lands a full body height away from the real eye.
    @Inject(
        method = "getViewBlockingState",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void gravitychanger$getViewBlockingState(
        LocalPlayer player, Frustum frustum, CallbackInfoReturnable<BlockState> cir
    ) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) return;

        if (player.noPhysics) {
            cir.setReturnValue(null);
            return;
        }

        Vec3 eyePos = player.getEyePosition();
        AABB nearPlaneBB = frustum.getNearPlaneBounds().move(eyePos);

        Vec3 size = RotationUtil.vecPlayerToWorld(
            player.getBbWidth() * 0.8D, 0.1D * player.getScale(), player.getBbWidth() * 0.8D, gravityDirection
        );

        BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 8; i++) {
            double x = eyePos.x + ((i >> 0) % 2 - 0.5F) * size.x;
            double y = eyePos.y + ((i >> 1) % 2 - 0.5F) * size.y;
            double z = eyePos.z + ((i >> 2) % 2 - 0.5F) * size.z;
            testPos.set(x, y, z);
            BlockState state = player.level().getBlockState(testPos);
            if (state.getRenderShape() != RenderShape.INVISIBLE
                && state.isViewBlocking(player.level(), testPos, nearPlaneBB)) {
                cir.setReturnValue(state);
                return;
            }
        }
        cir.setReturnValue(null);
    }
}
