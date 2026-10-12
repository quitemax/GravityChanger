package gravity_changer;

import gravity_changer.rotation.RotationParametersRecord;
import gravity_changer.command.DirectionArgumentType;
import gravity_changer.command.GravityCommand;
import gravity_changer.command.LocalDirectionArgumentType;
import gravity_changer.config.GravityChangerConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GravityChangerMod implements ModInitializer {
	public static final String MOD_ID = "gravity_changer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceKey<@NotNull CreativeModeTab> GRAVITY_ITEM_GROUP_KEY =
            ResourceKey.create(
                BuiltInRegistries.CREATIVE_MODE_TAB.key(),
                Identifier.fromNamespaceAndPath(MOD_ID, "gravity_item_group")
            );
    public static CreativeModeTab GRAVITY_GROUP_TAB = FabricCreativeModeTab.builder()
                .icon(() -> new ItemStack(GravityChangerItems.GRAVITY_CHANGER_UP))
            .title(Component.translatable("itemGroup.gravity_changer.creative_mode_gravity_tab"))
            .build();

    public static ConfigHolder<GravityChangerConfig> configHolder;
    public static GravityChangerConfig config;

	@Override
	public void onInitialize() {
        Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            GRAVITY_ITEM_GROUP_KEY,
            GRAVITY_GROUP_TAB
        );

        GravityChangerItems.init();

        //config init @TODO: why like this?
        AutoConfig.register(GravityChangerConfig.class, GsonConfigSerializer::new);
        configHolder = AutoConfig.getConfigHolder(GravityChangerConfig.class);
        configHolder.registerSaveListener(
            (configHolder, gravityChangerConfig) -> {
                RotationParametersRecord.updateDefault();
                return InteractionResult.PASS;
        });
        config = configHolder.getConfig();

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) ->
                GravityCommand.register(dispatcher)
        );

        GravityChangerMobEffects.init();
        GravityChangerPotions.init();

        GravityChangerBlocks.init();
        GravityChangerBlockItems.init();

        GravityChangerBlockEntities.init();

        DirectionArgumentType.init();
        LocalDirectionArgumentType.init();
	}
}