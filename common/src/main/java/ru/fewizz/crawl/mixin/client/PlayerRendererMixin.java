package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.mixininterface.CrawlingState;
import ru.fewizz.crawl.mixininterface.PrevPoseState;

@Mixin(PlayerRenderer.class)
abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {

	PlayerRendererMixin() { super(null, null, 0.0F); }

	@Inject(method = "setupRotations", at = @At("HEAD"), cancellable = true)
	void setupCrawlTransformations(PlayerRenderState state, PoseStack matrixStack, float f, float g, CallbackInfo ci) {
		if (((CrawlingState) state).isCrawling()) {
			super.setupRotations(state, matrixStack, f, g);
			float pitch = state.swimAmount;
			float lerpedHalfPI = Mth.lerp(pitch, 0.0F, -90);
			matrixStack.translate(0, pitch/10F, 0);
			matrixStack.translate(0, 0, pitch*EntityType.PLAYER.getHeight()/2.0);
			matrixStack.mulPose(Axis.XP.rotationDegrees(lerpedHalfPI));
			ci.cancel();
		}
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	void onUpdateRenderState(AbstractClientPlayer e, PlayerRenderState state, float tickDelta, CallbackInfo ci) {
		boolean crawling =
			e.getSwimAmount(tickDelta) > 0 &&
			e.getPose() != Pose.SWIMMING && (
				e.getPose() == Crawl.Shared.CRAWLING ||
				((PrevPoseState) e).getPrevPose() == Crawl.Shared.CRAWLING ||
				((PrevPoseState) e).getPrevTickPose() == Crawl.Shared.CRAWLING
			);

		((CrawlingState) state).setCrawling(crawling);

		state.isCrouching &= !crawling;
	}

}
