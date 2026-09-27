package ru.fewizz.crawl;

import java.util.ServiceLoader;

public abstract class PlatformHelperService {

    private static final ServiceLoader<PlatformHelperService> loader = ServiceLoader.load(PlatformHelperService.class);

    public abstract boolean impl_isModLoaded(String modId);

    public static boolean isModLoaded(String modId) {
        return PlatformHelperService.loader.findFirst().get().impl_isModLoaded(modId);
    }

}
