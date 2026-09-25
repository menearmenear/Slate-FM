package menear.nclient.slate;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slate implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("slate");

    @Override
    public void onInitialize() {
        LOGGER.info("Slate v1 Initialized!");
    }
}
