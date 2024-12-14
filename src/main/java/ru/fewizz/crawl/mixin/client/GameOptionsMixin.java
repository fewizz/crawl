package ru.fewizz.crawl.mixin.client;

import net.minecraft.client.Options;
import ru.fewizz.crawl.CrawlClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class GameOptionsMixin {

	@Inject(method = "processDumpedOptions", at = @At("HEAD"))
	void processDumpedOptions(Options.OptionAccess visitor, CallbackInfo ci) {
		visitor.process("toggleCrawl", CrawlClient.crawlToggled);
	}

}
