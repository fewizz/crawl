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
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.CrawlClient;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(KeyboardInput.class)
abstract class KeyboardInputMixin extends Input {
	boolean crawl$state;

	@Inject(require = 1, method="tick", at=@At("HEAD"))
	void onTickBegin(CallbackInfo ci) {
		PlayerEntity player = MinecraftClient.getInstance().player;

		if(!CrawlClient.isAnimationOnly()) {
			if (CrawlClient.getKeyActivationType() == CrawlClient.KeyActivationType.HOLD)
				crawl$state = CrawlClient.crawlKey.isPressed();
			else if (CrawlClient.crawlKey.wasPressed())
				crawl$state = !crawl$state;
		}
		else {
			crawl$state = false;
		}

		boolean oldCrawlState = player.getPose() == Shared.CRAWLING;
		
		if(crawl$state != oldCrawlState) {
			MinecraftClient.getInstance().getNetworkHandler().sendPacket(
				new CustomPayloadC2SPacket(
					Crawl.CRAWL_IDENTIFIER,
					new PacketByteBuf(
						Unpooled.wrappedBuffer(new byte[] { (byte) (crawl$state ? 1 : 0)})
					)
				)
			);

			player.getDataTracker().set(Shared.CRAWLING_REQUEST, crawl$state);
		}
		
	}
	
	@Inject(require = 1, method="tick", at=@At("RETURN"))
	void onTickEnd(CallbackInfo ci) {
		if(MinecraftClient.getInstance().player.getPose() != Shared.CRAWLING)
			return;
		movementForward *= 0.25;
		movementSideways *= 0.25;
		sneaking = false;
	}
}