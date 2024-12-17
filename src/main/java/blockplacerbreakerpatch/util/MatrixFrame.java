package blockplacerbreakerpatch.util;

import net.minecraft.client.util.math.MatrixStack;

public class MatrixFrame implements AutoCloseable {
    private final MatrixStack matrixStack;

    private MatrixFrame(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
        matrixStack.push();
    }

    @Override
    public void close() {
        matrixStack.pop();
    }

    public static MatrixFrame of(MatrixStack matrixStack) {
        return new MatrixFrame(matrixStack);
    }
}
