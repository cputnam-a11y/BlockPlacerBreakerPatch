package blockplacerbreakerpatch.mixin;

import blockplacerbreakerpatch.screen.slot.ToolSlot;
import com.khazoda.breakerplacer.screen.BreakerScreenHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreakerScreenHandler.class)
@Debug(export = true)
public abstract class BreakerScreenHandlerMixin extends Generic3x3ContainerScreenHandler {
    public BreakerScreenHandlerMixin(int syncId, PlayerInventory playerInventory) {
        super(syncId, playerInventory);
    }

    /**
     * Redirects the creation of the tool {@link Slot} to return a custom {@link Slot} that accepts only tools.
     * <p>NOTE: is a redirect because this will not work if another mod tries to do it.</p>
     * @return a custom slot accepting only tools
     */
    @Redirect(
            method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/inventory/Inventory;III)Lnet/minecraft/screen/slot/Slot;",
                    ordinal = 1
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/inventory/Inventory;onOpen(Lnet/minecraft/entity/player/PlayerEntity;)V"
                    )
            )
    )
    private Slot redirectSlotCreation(Inventory inventory, int index, int x, int y) {
        return new ToolSlot(inventory, index, x, y);
    }

    /**
     *
     * @param player the player requesting the quick move
     * @param slot the slot to move from
     * @param cir a callback info returnable representing a return in {@link BreakerScreenHandler#quickMove(PlayerEntity, int)} right before the {@link net.minecraft.screen.ScreenHandler#insertItem(ItemStack, int, int, boolean)} call
     * <pre>{@code
     * if (slot >= 9 && slot != 54) {
     *     // <-- here
     *     if (!this.insertItem(itemStackToMove, 0, 9, false)) {
     *          return ItemStack.EMPTY;'
     *     }
     * } else ...    }</pre>
     * @param itemStackToMove a local variable the contains the itemstack to move
     */
    @Inject(
            method = "quickMove",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/khazoda/breakerplacer/screen/BreakerScreenHandler;insertItem(Lnet/minecraft/item/ItemStack;IIZ)Z",
                    ordinal = 1
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"
                    )
            ),
            cancellable = true
    )
    private void onBeforeTryMoveIntoBreaker(
            PlayerEntity player, int slot,
            CallbackInfoReturnable<ItemStack> cir,
            @Local(ordinal = 1) ItemStack itemStackToMove
    ) {
        if (this.insertItem(itemStackToMove, 54, 55, false)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
