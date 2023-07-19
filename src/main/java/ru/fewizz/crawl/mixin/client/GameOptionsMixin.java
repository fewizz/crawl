package ru.fewizz.crawl.mixin.client;

import net.minecraft.client.option.GameOptions;
import ru.fewizz.crawl.CrawlClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {

	@Inject(
		method = "accept",
		at = @At("HEAD")
	)
	void preAccept(GameOptions.Visitor visitor, CallbackInfo ci) {
		visitor.accept("toggleCrawl", CrawlClient.crawlToggled);
	}

}
