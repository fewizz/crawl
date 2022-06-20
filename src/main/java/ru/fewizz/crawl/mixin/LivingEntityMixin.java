package ru.fewizz.crawl.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.LivingEntity;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

	@Shadow
	private float leaningPitch;
	@Shadow
	private float lastLeaningPitch;

	public LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Redirect(
		require = 1,
		method = "updateLeaningPitch",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/entity/LivingEntity.isInSwimmingPose()Z"
		)
	)
	boolean isInSwimmingOrCrawlingPose(LivingEntity ths) {
		return ths.isInSwimmingPose() || ths.getPose() == Shared.CRAWLING;
	}
}
