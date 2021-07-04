package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
	public ClientPlayerEntityMixin(ClientWorld cw, GameProfile gp) {
		super(cw, gp);
	}

	int ticksSinceLastSneaking = 0;
	
	@Inject(require = 1, method="tickMovement", at=@At(value="INVOKE", target="net/minecraft/client/network/AbstractClientPlayerEntity.tickMovement()V"))
	public void beforeSuperMovementTick(CallbackInfo ci) {
		boolean inCrawlingPose = getPose() == Crawl.Shared.CRAWLING;
		boolean crawlingAllowed = ticksSinceLastSneaking > 0 && ticksSinceLastSneaking < 7;
		boolean sneaking = MinecraftClient.getInstance().options.keySneak.isPressed();
		boolean wantsToCrawl = sneaking && (crawlingAllowed || inCrawlingPose);

		if(wantsToCrawl != inCrawlingPose) {
			MinecraftClient mc = MinecraftClient.getInstance();
			
			mc.getNetworkHandler().sendPacket(
				new CustomPayloadC2SPacket(
					Crawl.CRAWL_IDENTIFIER,
					new PacketByteBuf(Unpooled.copyBoolean(wantsToCrawl))
				)
			);

			getDataTracker().set(Shared.CRAWLING_REQUEST, wantsToCrawl);
		}

		if(!sneaking) {
			ticksSinceLastSneaking++;
		}
		else {
			ticksSinceLastSneaking = 0;
		}

		if(getPose() == Shared.CRAWLING)
			setSprinting(false);
	}
}