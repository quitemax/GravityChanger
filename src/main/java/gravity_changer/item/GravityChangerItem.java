package gravity_changer.item;

import gravity_changer.GravityChangerMod;
//import gravity_changer.api.GravityChangerAPI;
//import net.fabricmc.api.EnvType;
//import net.fabricmc.api.Environment;
//import net.minecraft.ChatFormatting;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResultHolder;
//import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.include.com.google.common.base.Function;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.TooltipFlag;
//import net.minecraft.world.level.Level;
//import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.resources.Identifier.fromNamespaceAndPath;

public class GravityChangerItem extends Item {

    public final Direction gravityDirection;
    
    public GravityChangerItem(Properties settings, Direction _gravityDirection) {
        super(settings);
        gravityDirection = _gravityDirection;
    }

    @Override
    public @NotNull InteractionResult use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand interactionHand
    ) {
        if (!level.isClientSide()) {
            GravityChangerAPI.setBaseGravityDirection(player, gravityDirection);
        }
//                return InteractionResultHolder.success(user.getItemInHand(hand));
        InteractionResult result = super.use(level, player,  interactionHand);

        return result;
    }
}
