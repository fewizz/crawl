package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import ru.fewizz.crawl.CrawlClient;

@Mixin(ControlsScreen.class)
abstract class ControlsOptionsScreenMixin extends OptionsSubScreen {

	ControlsOptionsScreenMixin() { super(null, null, null); }

	@ModifyReturnValue(method = "options", at = @At("RETURN"))
	private static OptionInstance<?>[] getOptionsWithCrawl(OptionInstance<?>[] original, @Local Options gameOptions) {
		var options = Lists.newArrayList(original);
		options.add(options.indexOf(gameOptions.toggleCrouch()) + 1, CrawlClient.crawlToggled);
		return options.toArray(new OptionInstance<?>[]{});
	}

}
