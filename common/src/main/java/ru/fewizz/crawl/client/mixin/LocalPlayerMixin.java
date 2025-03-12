package ru.fewizz.crawl.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.Crawl.Shared;
import ru.fewizz.crawl.client.CrawlClient;

@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin extends AbstractClientPlayer {

	@Shadow public Input input;
	@Shadow protected int sprintTriggerTime;

	LocalPlayerMixin() { super(null, null); }

	@Inject(method = "aiStep", at = @At("HEAD"))
	public void aiStep(CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player.getPose() == Shared.CRAWLING) {
			this.sprintTriggerTime = 0;
			this.input.shiftKeyDown = false;
		}
	}

	@Inject(
		method = "aiStep",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/client/player/AbstractClientPlayer.aiStep()V"
		)
	)
	public void beforeSuperAiStep(CallbackInfo ci) {
		boolean wantsToCrawl = CrawlClient.key.isDown();

		if (wantsToCrawl != getEntityData().get(Shared.CRAWL_REQUEST)) {
			Crawl.crawlRequestPacket.accept(wantsToCrawl);
			getEntityData().set(Shared.CRAWL_REQUEST, wantsToCrawl);
		}

		if (getPose() == Shared.CRAWLING) {
			setSprinting(false);
		}
	}

}
