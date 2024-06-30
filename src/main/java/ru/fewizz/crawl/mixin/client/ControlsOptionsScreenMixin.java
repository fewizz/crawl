package ru.fewizz.crawl.mixin.client;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.collect.Lists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.fewizz.crawl.CrawlClient;

@Mixin(ControlsOptionsScreen.class)
abstract class ControlsOptionsScreenMixin extends GameOptionsScreen {

	public ControlsOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
		super(parent, gameOptions, title);
	}

	@Inject(method = "getOptions", at = @At("RETURN"), cancellable = true)
	private static void getOptionsWithCrawl(GameOptions gameOptions, CallbackInfoReturnable<Object> cir) {
		var options = Lists.newArrayList((SimpleOption<?>[]) cir.getReturnValue());
		options.add(options.indexOf(gameOptions.getSneakToggled()) + 1, CrawlClient.crawlToggled);
		cir.setReturnValue(options.toArray(new SimpleOption[]{}));
	}

}
