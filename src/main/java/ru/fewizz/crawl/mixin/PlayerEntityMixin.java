package ru.fewizz.crawl.mixin;

import net.minecraft.entity.player.PlayerAbilities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.ShapeType;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {

	@Shadow @Final private PlayerAbilities abilities;

	public PlayerEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(
		require = 1,
		method = "initDataTracker",
		at = @At("HEAD")
	)
	public void onInitDataTracker(CallbackInfo ci) {
		getDataTracker().startTracking(Crawl.Shared.CRAWL_REQUEST, false);
	}
	
	@Redirect(
		require = 1,
		method = "updatePose",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/entity/player/PlayerEntity.setPose(Lnet/minecraft/entity/EntityPose;)V"
		)
	)
	public void onPreSetPose(PlayerEntity player, EntityPose pose) {
		if (!player.isFallFlying() && !this.isSpectator() && !this.hasVehicle() && !this.abilities.flying) {
			boolean requested = player.getDataTracker().get(Shared.CRAWL_REQUEST);

			boolean swimming = player.isSwimming() || player.isTouchingWater();

			if (requested) {
				if (!swimming) {
					pose = Shared.CRAWLING;
				}
				else {
					pose = EntityPose.SWIMMING;
				}
			}
			else if (pose == EntityPose.SWIMMING && !swimming) {
				pose = Shared.CRAWLING;
			}
		}

		setPose(pose);
	}

	@Inject(require = 1, method = "getDimensions", at = @At("HEAD"), cancellable = true)
	public void onGetDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> ci) {
		if (pose == Crawl.Shared.CRAWLING)
			ci.setReturnValue(Crawl.Shared.CRAWLING_DIMENSIONS);
	}

	@Inject(require = 1, method = "getActiveEyeHeight", at = @At("HEAD"), cancellable = true)
	public void onGetActiveEyeHeight(EntityPose pose, EntityDimensions size, CallbackInfoReturnable<Float> ci) {
		if (pose == Crawl.Shared.CRAWLING || size == Crawl.Shared.CRAWLING_DIMENSIONS)
			ci.setReturnValue(0.6F);
	}
}