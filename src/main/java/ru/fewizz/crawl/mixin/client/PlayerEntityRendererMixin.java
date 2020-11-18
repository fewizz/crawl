package ru.fewizz.crawl.mixin.client;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fewizz.crawl.Crawl.Shared;
import ru.fewizz.crawl.CrawlingInfo;
import ru.fewizz.crawl.PrevPoseInfo;

@Mixin(PlayerEntityRenderer.class)
abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	public PlayerEntityRendererMixin(EntityRenderDispatcher dispatcher, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
		super(dispatcher, model, shadowRadius);
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
		if( ((CrawlingInfo)getModel()).isCrawling() ) {
			super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
			float pitch = abstractClientPlayerEntity.getLeaningPitch(h);
			float lerpedHalfPI = MathHelper.lerp(pitch, 0.0F, -90);
			matrixStack.translate(0, pitch/10F, 0);
			matrixStack.translate(0, 0, pitch*abstractClientPlayerEntity.getEyeHeight(EntityPose.STANDING)/2.0);
			matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(lerpedHalfPI));
			ci.cancel();
		}
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
	void resetCrawlStateBeforeArmRendering(CallbackInfo ci) {
		((CrawlingInfo)getModel()).setCrawling(false);
	}

	@Inject(require = 1, method = "setModelPose", at = @At(value = "RETURN"))
	void setCrawlState(AbstractClientPlayerEntity abstractClientPlayerEntity, CallbackInfo ci) {
		((CrawlingInfo)getModel()).setCrawling(
			getModel().leaningPitch > 0
			&&
			(
				abstractClientPlayerEntity.getPose() == Shared.CRAWLING
				||
				((PrevPoseInfo)abstractClientPlayerEntity).getPrevPose() == Shared.CRAWLING
			)
		);
	}
}