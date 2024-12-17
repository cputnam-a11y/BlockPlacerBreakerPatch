package blockplacerbreakerpatch;

import blockplacerbreakerpatch.duck.BreakerBlockEntityExtension;
import blockplacerbreakerpatch.renderer.BreakerBlockEntityConfigurationRenderer;
import com.khazoda.breakerplacer.registry.RegBlocks;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public class BlockPlacerBreakerPatchClient implements ClientModInitializer {
    public static final KeyBinding BIND = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.blockplacerbreakerpatch.configure",
                    InputUtil.UNKNOWN_KEY.getCode(),
                    "key.categories.blockplacerbreakerpatch"
            )
    );
    public static boolean IS_CONFIGURING = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {
                    if (BIND.wasPressed()) {
                        IS_CONFIGURING = !IS_CONFIGURING;
                        //noinspection StatementWithEmptyBody
                        while (BIND.wasPressed()) ;
                    }
                }
        );
        WorldRenderEvents.END.register(new BreakerBlockEntityConfigurationRenderer());
        HudRenderCallback.EVENT.register((context, renderTickCounter) -> {
                    if (!MinecraftClient.isHudEnabled() || MinecraftClient.getInstance().inGameHud.getDebugHud().shouldShowDebugHud() || !IS_CONFIGURING) {
                        return;
                    }
                    context.drawText(
                            MinecraftClient.getInstance().textRenderer,
                            "CONFIGURING BLOCK BREAKERS",
                            2,
                            2,
                            0xFFFFFF,
                            false

                    );
                }
        );
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(ClientCommandManager.literal("testtest").executes(
                    context -> {
                        var client = context.getSource().getClient();
                        if (client.crosshairTarget instanceof BlockHitResult bhr) {
                            var world = client.world;
                            if (world.getBlockEntity(bhr.getBlockPos()) instanceof BreakerBlockEntityExtension bbee) {
                                context.getSource().sendFeedback(Text.of(bbee.blockPlacerBreakerPatch$getBehavior(bhr.getSide()).toString()));
                                return 1;
                            } else {
                                context.getSource().sendFeedback(Text.of("Not a BreakerBlockEntity"));
                                return 0;
                            }
                        } else {
                            context.getSource().sendFeedback(Text.of("Not looking at a block"));
                            return 0;
                        }
                    }
            ));
        });
    }
}
