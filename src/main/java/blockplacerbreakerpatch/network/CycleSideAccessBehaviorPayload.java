package blockplacerbreakerpatch.network;

import blockplacerbreakerpatch.BlockPlacerBreakerPatch;
import blockplacerbreakerpatch.duck.BreakerBlockEntityExtension;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public record CycleSideAccessBehaviorPayload(BlockPos pos, Direction direction, RegistryKey<World> world, K k) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, CycleSideAccessBehaviorPayload> PACKET_CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC,
                    CycleSideAccessBehaviorPayload::pos,
                    Direction.PACKET_CODEC,
                    CycleSideAccessBehaviorPayload::direction,
                    RegistryKey.createPacketCodec(RegistryKeys.WORLD),
                    CycleSideAccessBehaviorPayload::world,
                    PacketCodecs.STRING.xmap(K::valueOf, K::name),
                    CycleSideAccessBehaviorPayload::k,
                    CycleSideAccessBehaviorPayload::new
            );
    public static final Id<CycleSideAccessBehaviorPayload> ID = new Id<>(Identifier.of(BlockPlacerBreakerPatch.MOD_ID, "cycle_side_access_behavior"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    @SuppressWarnings({"resource", "DataFlowIssue"})
    public static void register() {
        PayloadTypeRegistry.playC2S().register(
                ID,
                PACKET_CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(
                ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        if (context.server().getWorld(payload.world()) != null && context.server().getWorld(payload.world()).getBlockEntity(payload.pos()) instanceof BreakerBlockEntityExtension bbee) {
                            var current = bbee.blockPlacerBreakerPatch$getBehavior(payload.direction());
                            var next = switch (payload.k()) {
                                case DIR -> new Pair<>(current.getFirst(), current.getSecond().next());
                                case INV -> new Pair<>(current.getFirst().next(), current.getSecond());
                            };
                            bbee.blockPlacerBreakerPatch$setBehavior(payload.direction(), next);
                            switch (payload.k()) {
                                case DIR ->
                                        context.player().sendMessage(Text.of("Direction access behavior for " + payload.direction() + " set to " + bbee.blockPlacerBreakerPatch$getBehavior(payload.direction()).getSecond()), true);
                                case INV ->
                                        context.player().sendMessage(Text.of("Side access behavior for " + payload.direction() + " set to " + bbee.blockPlacerBreakerPatch$getBehavior(payload.direction()).getFirst()), true);
                            }
                        }
                    });
                }
        );
    }

    public enum K {
        DIR,
        INV
    }
}
