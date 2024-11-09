package ru.fewizz.crawl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@WrapOperation(
		method = "updateLeaningPitch",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/entity/LivingEntity.isInSwimmingPose()Z"
		)
	)
	boolean isInSwimmingOrCrawlingPose(LivingEntity instance, Operation<Boolean> original) {
		return original.call(instance) || instance.getPose() == Shared.CRAWLING;
	}

}
