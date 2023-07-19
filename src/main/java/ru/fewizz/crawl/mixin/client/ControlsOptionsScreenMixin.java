package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.fewizz.crawl.CrawlClient;

@Mixin(ControlsOptionsScreen.class)
class ControlsOptionsScreenMixin extends GameOptionsScreen {
	
	public ControlsOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
		super(parent, gameOptions, title);
	}

	@ModifyArgs(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;createWidget(Lnet/minecraft/client/option/GameOptions;III)Lnet/minecraft/client/gui/widget/ClickableWidget;", ordinal = 0))
	void shrinkSneak(Args args){
		args.set(3, 100);
	}

	@ModifyArgs(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;createWidget(Lnet/minecraft/client/option/GameOptions;III)Lnet/minecraft/client/gui/widget/ClickableWidget;", ordinal = 1))
	void shrinkSprint(Args args){
		args.set(1, (int)args.get(1) - 55);
		args.set(3, 100);
	}

	@Inject(
		method = "init",
		at = @At("TAIL")
	)
	void postInit(CallbackInfo ci) {
		this.addDrawableChild(
			CrawlClient.crawlToggled.createWidget(
				this.gameOptions,
				this.width / 2 - 155 + (105)*2,
				this.height / 6 - 12 + 24,
				100
			)
		);
	}
}
