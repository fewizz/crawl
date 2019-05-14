package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import ru.fewizz.crawl.CrawlMod.Shared;

@Mixin(ClientPlayerEntity.class)
abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
	
	public MixinClientPlayerEntity(ClientWorld clientWorld_1, GameProfile gameProfile_1) {
		super(clientWorld_1, gameProfile_1);
	}
	
	@Inject(method="tickMovement", at=@At(value="INVOKE", target="net/minecraft/client/network/AbstractClientPlayerEntity.tickMovement()V"))
	public void beforeSuperMovementTick(CallbackInfo ci) {
		if(getPose() == Shared.CRAWLING)
			setSprinting(false);
	}
}