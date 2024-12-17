package blockplacerbreakerpatch.mixin.client;

import blockplacerbreakerpatch.duck.MatrixStackExtension;
import blockplacerbreakerpatch.util.MatrixFrame;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MatrixStack.class)
public class MatrixStackMixin implements MatrixStackExtension {

    @SuppressWarnings("DataFlowIssue")
    @Override
    public MatrixFrame frame() {
        return MatrixFrame.of((MatrixStack) (Object) this);
    }
}
