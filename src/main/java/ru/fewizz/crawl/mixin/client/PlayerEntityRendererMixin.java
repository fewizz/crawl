package ru.fewizz.crawl.mixin.client;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fewizz.crawl.Crawl.Shared;
import ru.fewizz.crawl.WasCrawlingPrevTick;

@Mixin(PlayerEntityRenderer.class)
abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	public PlayerEntityRendererMixin(EntityRenderDispatcher dispatcher, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
		super(dispatcher, model, shadowRadius);
	}

	@Redirect(
		require = 1,
		method = "setupTransforms",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/client/network/AbstractClientPlayerEntity.getLeaningPitch(F)F"
		)
	)
	float getLeaningPitchRedirect(AbstractClientPlayerEntity p, float delta) {
		//float lp = p.getLeaningPitch(delta);
		return p.getLeaningPitch(delta);
		//return ((WasCrawlingPrevTick)getModel()).isCrawling() || (lp != 0 && !p.isInSwimmingPose()) ? 0 : lp;
	}

	@Inject(
		require = 1,
		method = "renderArm",
		at = @At(
			value = "INVOKE",
			shift = At.Shift.AFTER,
			target = "net/minecraft/client/render/entity/PlayerEntityRenderer.setModelPose(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)V"
		)
	)
	void onRednerArm(CallbackInfo ci) {
		((WasCrawlingPrevTick)getModel()).setCrawling(false);
	}

	@Inject(require = 1, method = "setModelPose", at = @At(value = "RETURN"))
	void onSetModelPose(AbstractClientPlayerEntity abstractClientPlayerEntity, CallbackInfo ci) {
		((WasCrawlingPrevTick)getModel()).setCrawling(abstractClientPlayerEntity.getPose() == Shared.CRAWLING);
	}
}
