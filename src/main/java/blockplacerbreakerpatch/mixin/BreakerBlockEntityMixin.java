package blockplacerbreakerpatch.mixin;

import blockplacerbreakerpatch.codec.ModCodecs;
import blockplacerbreakerpatch.duck.BreakerBlockEntityExtension;
import blockplacerbreakerpatch.util.SideDirectionAccessBehavior;
import blockplacerbreakerpatch.util.SideInventoryAccessBehavior;
import com.khazoda.breakerplacer.block.entity.BreakerBlockEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashMap;

@Mixin(BreakerBlockEntity.class)
@Debug(export = true)
public abstract class BreakerBlockEntityMixin extends BaseBlockEntityMixin implements BreakerBlockEntityExtension {
    @Shadow(remap = false)
    @Final
    private static int[] TOOL_SLOT;
    @Shadow(remap = false)
    @Final
    private static int[] INVENTORY_SLOTS;
    @Unique
    private static final int[] TOOL_AND_INVENTORY_SLOTS;
    @Unique
    private HashMap<Direction, Pair<SideInventoryAccessBehavior, SideDirectionAccessBehavior>> directionToSlotType;

    @Unique
    private static final int[] EMPTY = new int[0];

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initDefaultDirectionRules(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        directionToSlotType = new HashMap<>();
        for (Direction dir : Direction.values()) {
            directionToSlotType.put(dir, new Pair<>(SideInventoryAccessBehavior.DEFAULT, SideDirectionAccessBehavior.DEFAULT));
        }
        this.markDirty();
    }

    @Override
    protected void prefixed$readAdditionalData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        super.prefixed$readAdditionalData(nbt, registryLookup, ci);
        NbtElement map = nbt.get("DirectionToSlotType");
        directionToSlotType = ModCodecs.MAP_CODEC.decode(RegistryOps.of(NbtOps.INSTANCE, registryLookup), map).getOrThrow().getFirst();
    }

    @Override
    protected void prefixed$writeAdditionalData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        NbtElement map = ModCodecs.MAP_CODEC.encodeStart(RegistryOps.of(NbtOps.INSTANCE, registryLookup), directionToSlotType).getOrThrow();
        nbt.put("DirectionToSlotType", map);
        super.prefixed$writeAdditionalData(nbt, registryLookup, ci);
    }

    @Override
    protected NbtCompound attachInitialChunkNbt(NbtCompound original, RegistryWrapper.WrapperLookup registryLookup) {
        NbtElement map = ModCodecs.MAP_CODEC.encodeStart(RegistryOps.of(NbtOps.INSTANCE, registryLookup), directionToSlotType).getOrThrow();
        original.put("DirectionToSlotType", map);
        return original;
    }

    @Override
    public void blockPlacerBreakerPatch$setBehavior(Direction direction, Pair<SideInventoryAccessBehavior, SideDirectionAccessBehavior> behavior) {
        this.directionToSlotType.put(direction, behavior);
        this.markDirty();
        updateNearbyPlayers();
    }
    @Unique
    private void updateNearbyPlayers() {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            var packet = this.toUpdatePacket();
            for(ServerPlayerEntity player : serverWorld.getChunkManager().chunkLoadingManager.getPlayersWatchingChunk(new ChunkPos(this.getPos()))) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    @Override
    public Pair<SideInventoryAccessBehavior, SideDirectionAccessBehavior> blockPlacerBreakerPatch$getBehavior(Direction direction) {
        return this.directionToSlotType.get(direction);
    }

    @ModifyReturnValue(method = "getAvailableSlots", at = @At("RETURN"))
    private int[] modifyAvailableSlots(int[] original, Direction side) {
        var behavior = directionToSlotType.get(side).getFirst();
        return switch (behavior) {
            case DEFAULT -> original;
            case NONE -> EMPTY;
            case TOOL -> TOOL_SLOT;
            case INVENTORY -> INVENTORY_SLOTS;
            case TOOL_AND_INVENTORY -> TOOL_AND_INVENTORY_SLOTS;
        };
    }

    @ModifyReturnValue(method = "canInsert", at = @At("RETURN"))
    private boolean modifyCanInsert(boolean original, int slot, ItemStack stack, @Nullable Direction dir) {
        var behavior = directionToSlotType.get(dir);
        var dirControl = behavior.getSecond();
        return switch (behavior.getFirst()) {
            case DEFAULT -> dirControl.canInsert(original);
            case NONE -> false;
            case TOOL ->
                    Arrays.stream(TOOL_SLOT).anyMatch(i -> i == slot) && stack.contains(DataComponentTypes.TOOL) && dirControl.canInsert(original);
            case INVENTORY -> Arrays.stream(INVENTORY_SLOTS).anyMatch(i -> i == slot) && dirControl.canInsert(original);
            case TOOL_AND_INVENTORY -> {
                if (Arrays.stream(TOOL_SLOT).anyMatch(i -> i == slot)) {
                    yield stack.contains(DataComponentTypes.TOOL) && dirControl.canInsert(original);
                } else {
                    yield dirControl.canInsert(original);
                }
            }
        };
    }

    @ModifyReturnValue(method = "canExtract", at = @At("RETURN"))
    private boolean modifyCanExtract(boolean original, int slot, ItemStack stack, Direction dir) {
        var behavior = directionToSlotType.get(dir);
        var dirControl = behavior.getSecond();
        return switch (behavior.getFirst()) {
            case DEFAULT -> dirControl.canExtract(original);
            case NONE -> false;
            case TOOL -> Arrays.stream(TOOL_SLOT).anyMatch(i -> i == slot) && dirControl.canExtract(original);
            case INVENTORY ->
                    Arrays.stream(INVENTORY_SLOTS).anyMatch(i -> i == slot) && dirControl.canExtract(original);
            case TOOL_AND_INVENTORY ->
                    Arrays.stream(TOOL_AND_INVENTORY_SLOTS).anyMatch(i -> i == slot) && dirControl.canExtract(original);
        };
    }

    static {
        TOOL_AND_INVENTORY_SLOTS = new int[TOOL_SLOT.length + INVENTORY_SLOTS.length];
        System.arraycopy(TOOL_SLOT, 0, TOOL_AND_INVENTORY_SLOTS, 0, TOOL_SLOT.length);
        System.arraycopy(INVENTORY_SLOTS, 0, TOOL_AND_INVENTORY_SLOTS, TOOL_SLOT.length, INVENTORY_SLOTS.length);
    }
}
