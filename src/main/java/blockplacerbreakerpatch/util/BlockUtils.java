package blockplacerbreakerpatch.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class BlockUtils {
    public static List<ItemStack> getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, ItemStack tool) {
        CachedBlockPosition cachedPos = new CachedBlockPosition(world, pos, false);
        // don't drop anything if the tool we are not using the correct tool
        if (state.isToolRequired() && !tool.canBreak(cachedPos) && !tool.getItem().isCorrectForDrops(tool, state))
            return Collections.emptyList();
        LootContextParameterSet.Builder builder =
                new LootContextParameterSet.Builder(world)
                        .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                        .add(LootContextParameters.TOOL, tool)
                        // the BLOCK_STATE parameter is added by BlockState#getDroppedStacks, so it is technically not necessary,
                        // but why not set it anyway
                        .add(LootContextParameters.BLOCK_STATE, state)
                        .addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity);

        return state.getDroppedStacks(builder);
    }
}
