package ru.fewizz.crawl.compat.nea.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.player.AbstractClientPlayer;
import ru.fewizz.crawl.Crawl;

@Mixin(
	targets = {"dev.tr7zw.notenoughanimations.animations.fullbody.CrawlingAnimation"},
	remap = false
)
public class CrawlingAnimationMixin {

	@ModifyReturnValue(method = "isValid", at = @At("RETURN"), remap = false)
	boolean isValid(boolean original, @Local(argsOnly = true) AbstractClientPlayer entity) {
		return original || entity.hasPose(Crawl.Shared.CRAWLING);
	}

}
