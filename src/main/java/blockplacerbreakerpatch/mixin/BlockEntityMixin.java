package blockplacerbreakerpatch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    @Shadow public abstract void markDirty();

    @Shadow @Nullable public abstract World getWorld();

    @Shadow public abstract BlockPos getPos();

    @ModifyReturnValue(method = "toInitialChunkDataNbt", at = @At("TAIL"))
    protected NbtCompound attachInitialChunkNbt(NbtCompound original, RegistryWrapper.WrapperLookup registryLookup) {
        return original;
    }
}
