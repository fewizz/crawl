package ru.fewizz.crawl.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.PacketByteBuf;
import ru.fewizz.crawl.CrawlMod;
import ru.fewizz.crawl.CrawlMod.Client;
import ru.fewizz.crawl.CrawlMod.Shared;

@Mixin(KeyboardInput.class)
abstract class MixinKeyboardInput extends Input {
	@Inject(method="tick", at=@At("HEAD"))
	void onTickBegin(CallbackInfo ci) {
		// Why it's here? ah, ok, nevermind..
		PlayerEntity p = MinecraftClient.getInstance().player;
		boolean newCrawlState = Client.keyCrawl.isPressed();
		if(newCrawlState != Shared.isCrawling(p)) {
			if(Shared.trySetCrawling(p, newCrawlState))
				MinecraftClient.getInstance().getNetworkHandler().sendPacket(
					new CustomPayloadC2SPacket(
						CrawlMod.CRAWL_IDENTIFIER,
						new PacketByteBuf(Unpooled.wrappedBuffer(new byte[] { (byte) (newCrawlState ? 1 : 0)}))
					)
				);
		}
	}
	
	@Inject(method="tick", at=@At("RETURN"))
	void onTickEnd(CallbackInfo ci) {
		if(!Shared.isCrawling(MinecraftClient.getInstance().player))
			return;
		movementForward *= 0.3;
		movementSideways *= 0.3;
	}
	
	@Redirect(
		method="tick",
		at=@At(
			value="FIELD",
			opcode=Opcodes.PUTFIELD,
			target="net/minecraft/client/input/KeyboardInput.sneaking:Z"
		),
		require=1
	)
	void onSneakingStateSave(KeyboardInput thisO, boolean sneakingState) {
		this.sneaking = sneakingState && !Shared.isCrawling(MinecraftClient.getInstance().player);
	}
}