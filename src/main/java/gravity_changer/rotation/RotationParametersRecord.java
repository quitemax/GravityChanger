package gravity_changer.rotation;

import gravity_changer.GravityChangerMod;
import net.minecraft.nbt.CompoundTag;

public record RotationParametersRecord(
    boolean rotateVelocity,
    boolean rotateView, // currently ignores this
    int rotationTimeMS
) {
    public static RotationParametersRecord defaultParam = new RotationParametersRecord(
        true, true, 500
    );
    
    public static void updateDefault() {
        defaultParam = new RotationParametersRecord(
            !GravityChangerMod.config.worldVelocity,
            true,
            GravityChangerMod.config.rotationTime
        );
    }
    
    public static RotationParametersRecord getDefault() {
        return defaultParam;
    }
    
    public RotationParametersRecord withRotationTimeMs(int rotationTimeMS) {
        return new RotationParametersRecord(
            rotateVelocity,
            rotateView,
            rotationTimeMS
        );
    }
    
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("RotateVelocity", rotateVelocity);
        tag.putBoolean("RotateView", rotateView);
        tag.putInt("RotationTimeMS", rotationTimeMS);
        return tag;
    }
    
    public static RotationParametersRecord fromTag(CompoundTag tag) {
        return new RotationParametersRecord(
            tag.getBoolean("RotateVelocity").get(),
            tag.getBoolean("RotateView").get(),
            tag.getInt("RotationTimeMS").get()
        );
    }
}
