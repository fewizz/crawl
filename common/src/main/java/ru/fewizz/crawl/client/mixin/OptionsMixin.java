package ru.fewizz.crawl.client.mixin;

import net.minecraft.client.Options;
import ru.fewizz.crawl.client.CrawlClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {

	@Inject(method = "processDumpedOptions", at = @At("HEAD"))
	void processDumpedOptions(Options.OptionAccess visitor, CallbackInfo ci) {
		visitor.process("toggleCrawl", CrawlClient.crawlToggled);
	}

}
