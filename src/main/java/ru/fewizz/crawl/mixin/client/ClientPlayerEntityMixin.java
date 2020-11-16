package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
	
	public ClientPlayerEntityMixin(ClientWorld cw, GameProfile gp) {
		super(cw, gp);
	}
	
	@Inject(require = 1, method="tickMovement", at=@At(value="INVOKE", target="net/minecraft/client/network/AbstractClientPlayerEntity.tickMovement()V"))
	public void beforeSuperMovementTick(CallbackInfo ci) {
		if(getPose() == Shared.CRAWLING)
			setSprinting(false);
	}
}