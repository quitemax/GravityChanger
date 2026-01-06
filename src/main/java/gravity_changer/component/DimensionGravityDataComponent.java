package gravity_changer.component;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import gravity_changer.GravityChangerComponents;
import net.minecraft.world.level.Level;

public class DimensionGravityDataComponent implements Component, AutoSyncedComponent {
    double dimensionGravityStrength = 1;
    
    private final Level currentWorld;
    
    public DimensionGravityDataComponent(Level world) {
        this.currentWorld = world;
    }
    
    public double getDimensionGravityStrength() {
        return dimensionGravityStrength;
    }
    
    public void setDimensionGravityStrength(double strength) {
        if (!currentWorld.isClientSide()) {
            dimensionGravityStrength = strength;
            GravityChangerComponents.DIMENSION_COMP_KEY.sync(currentWorld);
        }
    }

    @Override
    public void readData(ValueInput readView) {
        dimensionGravityStrength = readView.getDoubleOr("DimensionGravityStrength", 1.0);
    }

    @Override
    public void writeData(ValueOutput writeView) {
        writeView.putDouble("DimensionGravityStrength", dimensionGravityStrength);
    }
}
