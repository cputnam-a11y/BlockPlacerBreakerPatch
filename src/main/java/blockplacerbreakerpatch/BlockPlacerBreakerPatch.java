package blockplacerbreakerpatch;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockPlacerBreakerPatch implements ModInitializer {
    public static final String MOD_ID = "blockplacerbreakerpatch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Block Placer Breaker Patch");
    }
}