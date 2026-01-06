package gravity_changer.item;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;

public class GravityAnchorItem extends Item {
    public final Direction gravityDirection;

    public GravityAnchorItem(Properties settings, Direction _gravityDirection) {
        super(settings);
        gravityDirection = _gravityDirection;
    }
}
