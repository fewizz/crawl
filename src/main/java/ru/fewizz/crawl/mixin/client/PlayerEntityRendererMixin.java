package ru.fewizz.crawl.mixin.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.CrawlingState;
import ru.fewizz.crawl.PrevPoseInfo;

@Mixin(PlayerEntityRenderer.class)
abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
		super(ctx, model, shadowRadius);
	}

	@Inject(
		require = 1,
		cancellable = true,
		method = "setupTransforms",
		at = @At(
			value = "HEAD"
		)
	)
	void setupCrawlTransformations(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h, CallbackInfo ci) {
		Object model = getModel();

		if((model instanceof CrawlingState) && ((CrawlingState)model).isCrawling() ) {
			super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
			float pitch = abstractClientPlayerEntity.getLeaningPitch(h);
			float lerpedHalfPI = MathHelper.lerp(pitch, 0.0F, -90);
			matrixStack.translate(0, pitch/10F, 0);
			matrixStack.translate(0, 0, pitch*EntityType.PLAYER.getHeight()/2.0);
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(lerpedHalfPI));
			ci.cancel();
		}
	}
	
	@Inject(
		require = 1,
		method = "setModelPose",
		at = @At(value = "TAIL")
	)
	void onSetAttributes(AbstractClientPlayerEntity player, CallbackInfo ci) {
		if (!player.isSpectator()) {
			var model = getModel();
			((CrawlingState)model).setCrawling(
				player.getLeaningPitch(MinecraftClient.getInstance().getTickDelta()) > 0 &&
	 			player.getPose() != EntityPose.SWIMMING &&
	 			(
	 				player.getPose() == Crawl.Shared.CRAWLING ||
	 				((PrevPoseInfo)player).getPrevPose() == Crawl.Shared.CRAWLING ||
	 				((PrevPoseInfo)player).getPrevTickPose() == Crawl.Shared.CRAWLING
	 			)
	 		);
		}
	}
}
