package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.Crawl.Shared;
import ru.fewizz.crawl.CrawlClient;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}
	
	@Inject(
		require = 1,
		method="tickMovement",
		at=@At(value="INVOKE", target="net/minecraft/client/network/AbstractClientPlayerEntity.tickMovement()V")
	)
	public void beforeSuperMovementTick(CallbackInfo ci) {
		boolean wantsToCrawl = CrawlClient.key.isPressed();

		if(wantsToCrawl != getDataTracker().get(Shared.CRAWL_REQUEST)) {
			MinecraftClient mc = MinecraftClient.getInstance();
			
			mc.getNetworkHandler().sendPacket(
				new CustomPayloadC2SPacket(
					Crawl.CRAWL_IDENTIFIER,
					new PacketByteBuf(Unpooled.copyBoolean(wantsToCrawl))
				)
			);

			getDataTracker().set(Shared.CRAWL_REQUEST, wantsToCrawl);
		}

		if(getPose() == Shared.CRAWLING)
			setSprinting(false);
	}
}