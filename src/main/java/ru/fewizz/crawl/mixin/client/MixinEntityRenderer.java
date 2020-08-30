package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import ru.fewizz.crawl.CrawlMod.Shared;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
	
	@Redirect(
		method="renderLabelIfPresent(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V",
		at=@At(
			value="INVOKE",
			target="net/minecraft/entity/Entity.isSneaky()Z"
		)
	)
	boolean onGetIsInSneakingPose(Entity e) {
		return e.isInSneakingPose() || e.getPose() == Shared.CRAWLING;
	}
}
