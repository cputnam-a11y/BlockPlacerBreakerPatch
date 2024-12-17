package blockplacerbreakerpatch.codec;

import blockplacerbreakerpatch.util.SideDirectionAccessBehavior;
import blockplacerbreakerpatch.util.SideInventoryAccessBehavior;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.function.Function;

public class ModCodecs {
    public static final Codec<Pair<SideInventoryAccessBehavior, SideDirectionAccessBehavior>> PAIR_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                            SideInventoryAccessBehavior.CODEC.fieldOf("inventory").forGetter(Pair::getFirst),
                            SideDirectionAccessBehavior.CODEC.fieldOf("direction").forGetter(Pair::getSecond)
                    ).apply(instance, Pair::of)
            );
    public static final Codec<HashMap<Direction, Pair<SideInventoryAccessBehavior, SideDirectionAccessBehavior>>> MAP_CODEC =
            Codec.unboundedMap(
                    Direction.CODEC,
                    PAIR_CODEC

            ).xmap(
                    HashMap::new,
                    Function.identity()
            );
}
