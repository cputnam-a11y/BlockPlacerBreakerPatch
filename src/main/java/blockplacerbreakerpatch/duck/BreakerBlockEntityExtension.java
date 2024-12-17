package blockplacerbreakerpatch.duck;

import blockplacerbreakerpatch.util.SideDirectionAccessBehavior;
import blockplacerbreakerpatch.util.SideInventoryAccessBehavior;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.Direction;

public interface BreakerBlockEntityExtension {
    void blockPlacerBreakerPatch$setBehavior(Direction direction, Pair<SideInventoryAccessBehavior, SideDirectionAccessBehavior> behavior);
    Pair<SideInventoryAccessBehavior, SideDirectionAccessBehavior> blockPlacerBreakerPatch$getBehavior(Direction direction);
}
