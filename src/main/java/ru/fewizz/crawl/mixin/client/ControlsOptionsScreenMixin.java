package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import ru.fewizz.crawl.CrawlClient;

@Mixin(ControlsOptionsScreen.class)
abstract class ControlsOptionsScreenMixin extends GameOptionsScreen {

	ControlsOptionsScreenMixin() { super(null, null, null); }

	@WrapMethod(method = "getOptions")
	private static SimpleOption<?>[] getOptionsWithCrawl(GameOptions gameOptions, Operation<SimpleOption<?>[]> original) {
		var options = Lists.newArrayList(original.call(gameOptions));
		options.add(options.indexOf(gameOptions.getSneakToggled()) + 1, CrawlClient.crawlToggled);
		return options.toArray(new SimpleOption<?>[]{});
	}

}
