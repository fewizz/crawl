package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(KeyboardInput.class)
abstract class KeyboardInputMixin extends Input {
	
	@SuppressWarnings("resource")
	@Inject(require = 1, method = "tick", at = @At("RETURN"))
	void onTickEnd(CallbackInfo ci) {
		if(MinecraftClient.getInstance().player.getPose() != Shared.CRAWLING)
			return;
		
		movementForward *= 0.25;
		movementSideways *= 0.25;
		sneaking = false;
	}
}