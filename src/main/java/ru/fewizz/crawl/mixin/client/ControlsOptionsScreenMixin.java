package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import ru.fewizz.crawl.CrawlClient;

@Mixin(ControlsOptionsScreen.class)
class ControlsOptionsScreenMixin extends GameOptionsScreen {
	
	public ControlsOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
		super(parent, gameOptions, title);
	}

	@Inject(
		method = "init",
		at = @At("TAIL")
	)
	void postInit(CallbackInfo ci) {
		this.addDrawableChild(
			CrawlClient.crawlToggled.createButton(
				this.gameOptions,
				this.width / 2 - 155 + 160,
				this.height / 6 - 12 + 24*2,
				150
			)
		);
	}
}
