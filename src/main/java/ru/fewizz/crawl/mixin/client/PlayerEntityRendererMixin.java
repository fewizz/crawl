package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.mixininterface.CrawlingState;
import ru.fewizz.crawl.mixininterface.PrevPoseState;

@Mixin(PlayerEntityRenderer.class)
abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityRenderState, PlayerEntityModel> {

	PlayerEntityRendererMixin() { super(null, null, 0.0F); }

	@Inject(method = "setupTransforms", at = @At("HEAD"), cancellable = true)
	void setupCrawlTransformations(PlayerEntityRenderState state, MatrixStack matrixStack, float f, float g, CallbackInfo ci) {
		if (((CrawlingState) state).isCrawling()) {
			super.setupTransforms(state, matrixStack, f, g);
			float pitch = state.leaningPitch;
			float lerpedHalfPI = MathHelper.lerp(pitch, 0.0F, -90);
			matrixStack.translate(0, pitch/10F, 0);
			matrixStack.translate(0, 0, pitch*EntityType.PLAYER.getHeight()/2.0);
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(lerpedHalfPI));
			ci.cancel();
		}
	}

	@Inject(method = "updateRenderState", at = @At("TAIL"))
	void onUpdateRenderState(AbstractClientPlayerEntity e, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
		boolean crawling =
			e.getLeaningPitch(tickDelta) > 0 &&
			e.getPose() != EntityPose.SWIMMING && (
				e.getPose() == Crawl.Shared.CRAWLING ||
				((PrevPoseState) e).getPrevPose() == Crawl.Shared.CRAWLING ||
				((PrevPoseState) e).getPrevTickPose() == Crawl.Shared.CRAWLING
			);

		((CrawlingState) state).setCrawling(crawling);

		state.sneaking &= !crawling;
		state.isInSneakingPose &= !crawling;
	}

}
