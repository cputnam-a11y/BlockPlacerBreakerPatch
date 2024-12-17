package blockplacerbreakerpatch.mixin;

import com.khazoda.breakerplacer.block.entity.BaseBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for {@link BaseBlockEntity}.
 * a mixin containing stub methods to allow overriding in {@link BreakerBlockEntityMixin}.
 */
@Mixin(BaseBlockEntity.class)
public abstract class BaseBlockEntityMixin extends LootableContainerBlockEntityMixin {
    @Shadow public abstract BlockEntityUpdateS2CPacket toUpdatePacket();

    @Inject(method = "readNbt", at = @At("TAIL"))
    protected void prefixed$readAdditionalData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    protected void prefixed$writeAdditionalData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
    }
}
