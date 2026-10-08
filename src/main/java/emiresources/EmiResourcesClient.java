package emiresources;

import net.fabricmc.api.ClientModInitializer;
import net.xiaoyu233.fml.ModResourceManager;

/**
 * Client entrypoint. Registers the resource domain so {@code assets/emiresources/lang/*.lang} is
 * picked up by FML's language loader.
 */
public class EmiResourcesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModResourceManager.addResourcePackDomain(EmiResources.MOD_ID);
    }
}
