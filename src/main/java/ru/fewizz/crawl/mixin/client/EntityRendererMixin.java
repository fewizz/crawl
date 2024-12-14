package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import ru.fewizz.crawl.mixininterface.CrawlingState;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	
	@ModifyExpressionValue(
		method = "renderNameTag("+
			"Lnet/minecraft/client/renderer/entity/state/EntityRenderState;" +
			"Lnet/minecraft/network/chat/Component;"+
			"Lcom/mojang/blaze3d/vertex/PoseStack;"+
			"Lnet/minecraft/client/renderer/MultiBufferSource;"+
			"I"+
		")V",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;isDiscrete:Z"
		)
	)
	boolean isSneaking(boolean sneaking, EntityRenderState state) {
		return sneaking || (state instanceof CrawlingState cs && cs.isCrawling());
	}

}
