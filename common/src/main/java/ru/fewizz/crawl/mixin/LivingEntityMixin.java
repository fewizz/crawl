package ru.fewizz.crawl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.entity.LivingEntity;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends net.minecraft.world.entity.Entity {

	LivingEntityMixin() { super(null, null); }

	@ModifyExpressionValue(
		method = "updateSwimAmount",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/world/entity/LivingEntity.isVisuallySwimming()Z"
		)
	)
	boolean updateSwimAmount(boolean isInSwimmingPose) {
		return isInSwimmingPose || this.getPose() == Shared.CRAWLING;
	}

}
