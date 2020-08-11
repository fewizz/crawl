package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import ru.fewizz.crawl.CrawlMod.Shared;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {
	
	@Redirect(method = "setupTransforms", at = @At(value = "INVOKE", target = "getLeaningPitch"))
	float getLeaningPitchRedirect(AbstractClientPlayerEntity p, float delta) {
		return p.getPose() == Shared.CRAWLING ? 0 : p.getLeaningPitch(delta);
	}
}
