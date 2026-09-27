package ru.fewizz.crawl.neoforge;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class PlatformHelperService extends ru.fewizz.crawl.PlatformHelperService {

    @Override
    public boolean impl_isModLoaded(String modId) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
    }

}
