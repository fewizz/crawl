package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import ru.fewizz.crawl.CrawlMod;
import ru.fewizz.crawl.CrawlMod.Client;
import ru.fewizz.crawl.CrawlMod.Shared;

@Mixin(KeyboardInput.class)
abstract class MixinKeyboardInput extends Input {
	@Inject(method="tick", at=@At("HEAD"))
	void onTickBegin(CallbackInfo ci) {
		// Why it's here? ah, ok, nevermind..
		PlayerEntity player = MinecraftClient.getInstance().player;
		
		boolean newCrawlState = Client.keyCrawl.isPressed();
		boolean oldCrawlState = player.getPose() == Shared.CRAWLING;
		
		if(newCrawlState != oldCrawlState) {
			MinecraftClient.getInstance().getNetworkHandler().sendPacket(
				new CustomPayloadC2SPacket(
					CrawlMod.CRAWL_IDENTIFIER,
					new PacketByteBuf(
						Unpooled.wrappedBuffer(new byte[] { (byte) (newCrawlState ? 1 : 0)})
					)
				)
			);
			player.getDataTracker().set(Shared.CRAWLING_REQUEST, newCrawlState);
		}
		
	}
	
	@Inject(method="tick", at=@At("RETURN"))
	void onTickEnd(CallbackInfo ci) {
		if(MinecraftClient.getInstance().player.getPose() != Shared.CRAWLING)
			return;
		movementForward *= 0.3;
		movementSideways *= 0.3;
		sneaking = false;
	}
}