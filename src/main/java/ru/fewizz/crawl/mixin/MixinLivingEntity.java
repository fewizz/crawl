package ru.fewizz.crawl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.fewizz.crawl.CrawlMod.Shared;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
	@Shadow
	private float leaningPitch;
	@Shadow
	private float lastLeaningPitch;

	@Redirect(method = "updateLeaningPitch", at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.isInSwimmingPose()Z"))
	boolean isInSwimmingPoseIn(LivingEntity ths) {
		return ths.isInSwimmingPose() || ths.getPose() == Shared.CRAWLING;
	}
}
