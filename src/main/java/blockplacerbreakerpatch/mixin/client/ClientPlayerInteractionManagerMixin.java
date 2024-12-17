package blockplacerbreakerpatch.mixin.client;

import blockplacerbreakerpatch.BlockPlacerBreakerPatchClient;
import blockplacerbreakerpatch.network.CycleSideAccessBehaviorPayload;
import com.khazoda.breakerplacer.registry.RegBlocks;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "interactBlock", at = @At(value = "NEW", target = "()Lorg/apache/commons/lang3/mutable/MutableObject;", remap = false), cancellable = true)
    private void beforeFabricInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (!player.getStackInHand(hand).isEmpty()) {
            return;
        }
        // does nothing in prod, but makes the java compiler happy
        assert client.world != null;
        if (client.world.getBlockState(hitResult.getBlockPos()).isOf(RegBlocks.BREAKER_BLOCK) && BlockPlacerBreakerPatchClient.IS_CONFIGURING) {
            ClientPlayNetworking.send(new CycleSideAccessBehaviorPayload(hitResult.getBlockPos(), hitResult.getSide(), client.world.getRegistryKey(), !player.isSneaking() ? CycleSideAccessBehaviorPayload.K.INV : CycleSideAccessBehaviorPayload.K.DIR));
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
