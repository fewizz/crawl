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
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.PlayerExtended;
import ru.fewizz.crawl.client.CrawlClient;
import ru.fewizz.crawl.client.mixininterface.CrawlingState;

@Mixin(PlayerRenderer.class)
abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {

	PlayerRendererMixin() { super(null, null, 0.0F); }

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	void onUpdateRenderState(AbstractClientPlayer e, PlayerRenderState state, float tickDelta, CallbackInfo ci) {
		boolean crawling =
			e.getPose() == Crawl.Shared.CRAWLING || (
				e.getSwimAmount(tickDelta) > 0 &&
				((PlayerExtended) e).crawl_getPrevPose() == Crawl.Shared.CRAWLING
			);

		((CrawlingState) state).setCrawling(CrawlClient.replaceAnimation ? crawling : false);
		state.isVisuallySwimming |= !CrawlClient.replaceAnimation && crawling;
		state.isCrouching &= !crawling;
	}

	// need this, to take ... } else if (h > 0.0F) { ... branch
	@ModifyExpressionValue(
		method = "setupRotations",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;swimAmount:F"
		)
	)
	float rotateWhenCrawlingSameWayAsWhenSwimming(float swimAmount, @Local PlayerRenderState state) {
		if (CrawlClient.replaceAnimation && ((CrawlingState) state).isCrawling()) {
			swimAmount += 0.0001F;  // evil :)
		}
		return swimAmount;
	}

	@ModifyExpressionValue(
		method = "setupRotations",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;isVisuallySwimming:Z"
		)
	)
	boolean applyXRotationWhenCrawling(boolean isVisuallySwimming, @Local PlayerRenderState state) {
		return (CrawlClient.replaceAnimation && ((CrawlingState) state).isCrawling()) || isVisuallySwimming;
	}

	@ModifyExpressionValue(method = "setupRotations", at = @At(value = "CONSTANT", args="floatValue=-1.0F"))
	float smootherYOffsetSwimmingPosTransition(float original, @Local PlayerRenderState state) {
		return Mth.lerp(state.swimAmount, 0.0F, original);
	}

	@ModifyExpressionValue(method = "setupRotations", at = @At(value = "CONSTANT", args="floatValue=0.3F"))
	float smootherZOffsetSwimmingPosTransition(float original, @Local PlayerRenderState state) {
		return Mth.lerp(state.swimAmount, 0.0F, original-0.1F);
	}

}
