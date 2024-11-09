package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import ru.fewizz.crawl.mixininterface.CrawlingState;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	
	@ModifyExpressionValue(
		method = "renderLabelIfPresent("+
			"Lnet/minecraft/client/render/entity/state/EntityRenderState;" +
			"Lnet/minecraft/text/Text;"+
			"Lnet/minecraft/client/util/math/MatrixStack;"+
			"Lnet/minecraft/client/render/VertexConsumerProvider;"+
			"I"+
		")V",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/render/entity/state/EntityRenderState;sneaking:Z"
		)
	)
	boolean onGetIsInSneakingPose(boolean sneaking, EntityRenderState state) {
		return sneaking || (state instanceof CrawlingState cs && cs.isCrawling());
	}

}
