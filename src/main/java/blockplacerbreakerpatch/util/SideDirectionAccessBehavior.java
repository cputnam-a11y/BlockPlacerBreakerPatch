package blockplacerbreakerpatch.util;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum SideDirectionAccessBehavior implements StringIdentifiable {
    NONE("NONE"),
    DEFAULT("DEFAULT"),
    EXTRACT("EXTRACT"),
    INSERT("INSERT");
    public static final Codec<SideDirectionAccessBehavior> CODEC =
            StringIdentifiable.createCodec(SideDirectionAccessBehavior::values);
    private final String name;

    SideDirectionAccessBehavior(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }

    public boolean canExtract(boolean defaultValue) {
        return switch (this) {
            case DEFAULT -> defaultValue;
            case EXTRACT -> true;
            case INSERT, NONE -> false;
        };
    }

    public boolean canInsert(boolean defaultValue) {
        return switch (this) {
            case DEFAULT -> defaultValue;
            case EXTRACT, NONE -> false;
            case INSERT -> true;
        };
    }

    public SideDirectionAccessBehavior next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
