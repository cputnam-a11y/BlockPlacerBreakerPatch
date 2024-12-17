package blockplacerbreakerpatch.mixin;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin extends BlockEntityMixin{
}
