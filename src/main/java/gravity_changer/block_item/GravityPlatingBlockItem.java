package gravity_changer.block_item;

//import net.fabricmc.fabric.api.item.v1.FabricItem.Settings;
//import net.minecraft.ChatFormatting;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Registry;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.chat.Component;
//import net.minecraft.world.InteractionResult;
import gravity_changer.block.GravityPlatingBlock;
import gravity_changer.block_entity.GravityPlatingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.TooltipFlag;
//import net.minecraft.world.item.context.BlockPlaceContext;S
//import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import org.jetbrains.annotations.Nullable;

//import java.util.List;

public class GravityPlatingBlockItem extends BlockItem {

    public GravityPlatingBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult place(@NotNull BlockPlaceContext context) {
        InteractionResult result = super.place(context);

        Level level = context.getLevel();
        ItemStack itemStack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (level.isClientSide()) {
            return result;
        }

        BlockEntity blockEntity = level.getBlockEntity(clickedPos);
        if (blockEntity instanceof GravityPlatingBlockEntity be) {
            be.onPlacing(context.getClickedFace().getOpposite(), itemStack);
        }

        return result;
    }

    @Override
    public @NotNull InteractionResult use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand interactionHand
    ) {
        InteractionResult result = super.use(level, player,  interactionHand);

        return result;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext useOnContext) {
        InteractionResult result = super.useOn(useOnContext);

        return result;
    }
}