package ru.fewizz.crawl.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.PlayerAbilities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.PrevPoseInfo;
import ru.fewizz.crawl.Crawl.Shared;

import java.util.Map;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PrevPoseInfo {

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow @Final private PlayerAbilities abilities;

	@Shadow @Final @Mutable
	private static Map<EntityPose, EntityDimensions> POSE_DIMENSIONS;
	@Unique
	EntityPose prevPose;
	
	@Unique
	EntityPose prevTickPose;

	@Inject(
		require = 1,
		method = "initDataTracker",
		at = @At("HEAD")
	)
	public void onInitDataTracker(CallbackInfo ci) {
		getDataTracker().startTracking(Crawl.Shared.CRAWL_REQUEST, false);
	}
	
	@ModifyArg(
		require = 1,
		method = "updatePose",
		index = 0,
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/entity/player/PlayerEntity.setPose(Lnet/minecraft/entity/EntityPose;)V"
		)
	)
	public EntityPose onPreSetPose(EntityPose pose) {
		if (!isFallFlying() && !this.isSpectator() && !this.hasVehicle() && !this.abilities.flying) {
			boolean requested = getDataTracker().get(Shared.CRAWL_REQUEST);

			boolean swimming = isSwimming() || isTouchingWater();

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

		return pose;
	}

	@Inject(require = 1, method = "<clinit>", at = @At("TAIL"))
	private static void onPoseMapCreation(CallbackInfo ci) {
		POSE_DIMENSIONS = ImmutableMap.<EntityPose, EntityDimensions>builder().putAll(POSE_DIMENSIONS).put(Crawl.Shared.CRAWLING, Crawl.Shared.CRAWLING_DIMENSIONS).build();
	}

	@Inject(require = 1, method = "getActiveEyeHeight", at = @At("HEAD"), cancellable = true)
	public void onGetActiveEyeHeight(EntityPose pose, EntityDimensions size, CallbackInfoReturnable<Float> ci) {
		if (pose == Crawl.Shared.CRAWLING || size == Crawl.Shared.CRAWLING_DIMENSIONS)
			ci.setReturnValue(0.6F);
	}
	
	@Inject(
		require = 1,
		method = "tick",
		at = @At(value = "TAIL")
	)
	public void onTickEnd(CallbackInfo ci) {
		if(getPose() != prevTickPose) {
			prevPose = prevTickPose;
		}
		prevTickPose = getPose();
	}

	@Override
	public EntityPose getPrevPose() {
		return prevPose;
	}
	
	@Override
	public EntityPose getPrevTickPose() {
		return prevTickPose;
	}
}