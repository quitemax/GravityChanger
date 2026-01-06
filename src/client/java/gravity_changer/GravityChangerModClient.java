package gravity_changer;

import gravity_changer.block.GravityPlatingBlock;
import gravity_changer.util.GCUtil;
import net.fabricmc.api.ClientModInitializer;

//import gravity_changer.plating.GravityPlatingBlock;
//import gravity_changer.util.GCUtil;
//import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GravityChangerModClient implements ClientModInitializer {

    private static final String ISSUE_LINK = "https://github.com/qouteall/GravityChanger/issues";
    private static boolean displayPreviewWarning = true;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ClientTickEvents.START_CLIENT_TICK.register(new ClientTickEvents.StartTick() {
            @Override
            public void onStartTick(@NotNull Minecraft client) {
                if (client.player == null) {
                    return;
                }
                if (displayPreviewWarning) {
                    displayPreviewWarning = false;
                    client.player.displayClientMessage(
                            Component.translatable("gravity_changer.preview"), true
                    );
                }
            }
        });


//        ChunkSectionLayer layer = ;

//        BlockRenderLayerMap.putBlocks(RenderType..cutout(), GravityChangerBlocks.PLATING_BLOCK);
	}
}