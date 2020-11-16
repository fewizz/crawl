package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	
	@Redirect(
		require = 1,
		method="renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
		at=@At(
			value="INVOKE",
			target="Lnet/minecraft/entity/Entity;isSneaky()Z"
		)
	)
	boolean onGetIsInSneakingPose(Entity e) {
		return e.isInSneakingPose() || e.getPose() == Shared.CRAWLING;
	}
}
