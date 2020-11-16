package ru.fewizz.crawl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.LivingEntity;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Shadow
	private float leaningPitch;
	@Shadow
	private float lastLeaningPitch;

	@Redirect(
		require = 1,
		method = "updateLeaningPitch",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/entity/LivingEntity.isInSwimmingPose()Z"
		)
	)
	boolean isInSwimmingPoseIn(LivingEntity ths) {
		return ths.isInSwimmingPose() || ths.getPose() == Shared.CRAWLING;
	}
}
