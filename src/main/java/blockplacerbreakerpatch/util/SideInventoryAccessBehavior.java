package blockplacerbreakerpatch.util;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum SideInventoryAccessBehavior implements StringIdentifiable {
    NONE("NONE"),
    DEFAULT("DEFAULT"),
    TOOL("TOOL"),
    INVENTORY("INVENTORY"),
    TOOL_AND_INVENTORY("TOOL_AND_INVENTORY");
    public static final Codec<SideInventoryAccessBehavior> CODEC =
            StringIdentifiable.createCodec(SideInventoryAccessBehavior::values);

    private final String name;

    SideInventoryAccessBehavior(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }

    public SideInventoryAccessBehavior next() {
        return values()[(ordinal() + 1) % values().length];
    }

    @Override
    public String asString() {
        return name;
    }
}
