package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.Crawl.Shared;
import ru.fewizz.crawl.CrawlClient;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

	@Shadow
	public Input input;

	@Shadow
	protected int ticksLeftToDoubleTapSprint;

	ClientPlayerEntityMixin() { super(null, null); }

	@Inject(method = "tickMovement", at = @At("HEAD"))
	public void beforeTickMovement(CallbackInfo ci) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player.getPose() == Shared.CRAWLING) {
			this.ticksLeftToDoubleTapSprint = 0;
		}
	}

	@Inject(
		method = "tickMovement",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/client/network/AbstractClientPlayerEntity.tickMovement()V"
		)
	)
	public void beforeSuperMovementTick(CallbackInfo ci) {
		boolean wantsToCrawl = CrawlClient.key.isPressed();

		if (wantsToCrawl != getDataTracker().get(Shared.CRAWL_REQUEST)) {
			ClientPlayNetworking.send(new Crawl.Payload(wantsToCrawl));
			getDataTracker().set(Shared.CRAWL_REQUEST, wantsToCrawl);
		}

		if (getPose() == Shared.CRAWLING) {
			setSprinting(false);
		}
	}

}
