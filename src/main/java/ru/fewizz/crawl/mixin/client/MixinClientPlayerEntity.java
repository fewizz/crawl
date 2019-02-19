package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import ru.fewizz.crawl.CrawlMod;
import ru.fewizz.crawl.CrawlMod.Shared;

@Mixin(ClientPlayerEntity.class)
abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
	
	public MixinClientPlayerEntity(ClientWorld clientWorld_1, GameProfile gameProfile_1) {
		super(clientWorld_1, gameProfile_1);
	}

	@Overwrite
	private boolean method_3150(BlockPos blockPos_1) {
		if (this.isSwimming() || CrawlMod.Shared.isCrawling(this)) 
			return !this.method_7326(blockPos_1);
      	else
	  		return !this.method_7352(blockPos_1);
      	
	}
	
	@Inject(method="updateMovement", at=@At(value="INVOKE", target="net/minecraft/client/network/AbstractClientPlayerEntity.updateMovement()V"))
	public void beforeSuperMovementUpdate(CallbackInfo ci) {
		if(Shared.isCrawling(this))
			setSprinting(false);
	}
}