package neresources;

import net.fabricmc.api.ClientModInitializer;
import net.xiaoyu233.fml.ModResourceManager;

/**
 * Client entrypoint. Registers the resource domain so {@code assets/neresources/lang/*.lang} is
 * picked up by FML's language loader.
 */
public class NEResourcesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModResourceManager.addResourcePackDomain(NEResources.MOD_ID);
    }
}
