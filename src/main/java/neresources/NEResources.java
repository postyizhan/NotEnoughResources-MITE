package neresources;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common entrypoint. Everything this mod shows is derived on demand by the EMI plugin, so there is
 * nothing to register here beyond the logger the rest of the mod uses.
 */
public class NEResources implements ModInitializer {
    public static final String MOD_ID = "neresources";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("NotEnoughResources loaded; waiting for EMI to request recipes.");
    }
}
