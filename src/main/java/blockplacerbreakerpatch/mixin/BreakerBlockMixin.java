package blockplacerbreakerpatch.mixin;

import blockplacerbreakerpatch.util.BlockUtils;
import com.khazoda.breakerplacer.block.BreakerBlock;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BreakerBlock.class)
@Debug(export = true)
public class BreakerBlockMixin {
    /**
     * Called right before the block is broken, to allow for tool damage to be applied.
     */
    @Inject(method = "activate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private void beforeBreakBlock(
            ServerWorld world, BlockState state,
            BlockPos pos, CallbackInfo ci,
            @Local ItemStack tool
    ) {
        tool.damage(1, world, null, (item) -> {
            // nothing needs to be done here?
        });
    }

    /**
     * Called to get the items that will be dropped when the block is broken.
     * effectively an {@link org.spongepowered.asm.mixin.Overwrite}, but I couldn't bring myself to do it.
     * Unconditionally returns at {@link org.spongepowered.asm.mixin.injection.points.MethodHead}.
     * returns the items that will really be dropped when the block is broken.
     */
    @Inject(method = "getDroppedStacks", at = @At("HEAD"), cancellable = true)
    public void getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> cir) {
         cir.setReturnValue(BlockUtils.getDroppedStacks(state, world, pos, blockEntity, tool));
    }
}
