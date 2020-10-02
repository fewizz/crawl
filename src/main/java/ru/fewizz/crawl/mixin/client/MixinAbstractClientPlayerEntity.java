package ru.fewizz.crawl.mixin.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import ru.fewizz.crawl.WasHeCrawlingPrevTickInfo;

@Mixin(AbstractClientPlayerEntity.class)
public class MixinAbstractClientPlayerEntity implements WasHeCrawlingPrevTickInfo {
    boolean wasCrawling = false;

    @Override
    public boolean wasHeCrawlingPrevTick() {
        return wasCrawling;
    }

    @Override
    public void setWasHeCrawlingPrevTick(boolean v) {
        wasCrawling = v;
    }
}
