package blockplacerbreakerpatch.duck;

import blockplacerbreakerpatch.util.MatrixFrame;

public interface MatrixStackExtension {
    default MatrixFrame frame() {
        throw new AssertionError();
    }
}
