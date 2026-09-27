package ru.fewizz.crawl.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class PlatformHelperService extends ru.fewizz.crawl.PlatformHelperService {

    @Override
    public boolean impl_isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

}
