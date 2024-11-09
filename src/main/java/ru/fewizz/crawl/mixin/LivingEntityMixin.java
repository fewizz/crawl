package ru.fewizz.crawl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

	LivingEntityMixin() { super(null, null); }

	@ModifyExpressionValue(
		method = "updateLeaningPitch",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/entity/LivingEntity.isInSwimmingPose()Z"
		)
	)
	boolean isInSwimmingOrCrawlingPose(boolean isInSwimmingPose) {
		return isInSwimmingPose || this.getPose() == Shared.CRAWLING;
	}

}
