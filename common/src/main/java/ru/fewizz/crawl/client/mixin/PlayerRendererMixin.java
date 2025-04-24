package ru.fewizz.crawl.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.client.CrawlClient;
import ru.fewizz.crawl.client.mixininterface.CrawlingState;
import ru.fewizz.crawl.mixininterface.PrevPoseState;

@Mixin(PlayerRenderer.class)
abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

	PlayerRendererMixin() { super(null, null, 0.0F); }

	@Inject(method = "setModelProperties", at = @At("TAIL"))
	void onUpdateRenderState(AbstractClientPlayer e, CallbackInfo ci) {
		boolean crawling =
			e.getPose() == Crawl.Shared.CRAWLING || (
				e.getSwimAmount(0.0F) > 0 &&
				((PrevPoseState) e).getPrevPose() == Crawl.Shared.CRAWLING
			);

		((CrawlingState) this.getModel()).setCrawling(CrawlClient.replaceAnimation ? crawling : false);
		getModel().crouching &= !crawling;
	}

	// need this, to take ... } else if (h > 0.0F) { ... branch
	@ModifyExpressionValue(
		method = "setupRotations",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/AbstractClientPlayer;getSwimAmount(F)F"
		)
	)
	float rotateWhenCrawlingSameWayAsWhenSwimming(float swimAmount) {
		if (CrawlClient.replaceAnimation && ((CrawlingState) this.getModel()).isCrawling()) {
			swimAmount += 0.0001F;  // evil :)
		}
		return swimAmount;
	}

	@ModifyExpressionValue(
		method = "setupRotations",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/AbstractClientPlayer;isVisuallySwimming()Z"
		)
	)
	boolean applyXRotationWhenCrawling(boolean isVisuallySwimming) {
		return (CrawlClient.replaceAnimation && ((CrawlingState) this.getModel()).isCrawling()) || isVisuallySwimming;
	}

	@ModifyExpressionValue(method = "setupRotations", at = @At(value = "CONSTANT", args="floatValue=-1.0F"))
	float smootherYOffsetSwimmingPosTransition(float original, @Local AbstractClientPlayer state, @Local(ordinal = 2) float pt) {
		return Mth.lerp(state.getSwimAmount(pt), 0.0F, original);
	}

	@ModifyExpressionValue(method = "setupRotations", at = @At(value = "CONSTANT", args="floatValue=0.3F"))
	float smootherZOffsetSwimmingPosTransition(float original, @Local AbstractClientPlayer state, @Local(ordinal = 2) float pt) {
		return Mth.lerp(state.getSwimAmount(pt), 0.0F, original-0.1F);
	}

}
