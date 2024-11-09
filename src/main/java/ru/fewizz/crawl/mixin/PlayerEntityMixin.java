package ru.fewizz.crawl.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.Crawl.Shared;
import ru.fewizz.crawl.mixininterface.PrevPoseState;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PrevPoseState {

	PlayerEntityMixin() { super(null, null); }

	@Shadow @Final
	private PlayerAbilities abilities;

	@Shadow @Final @Mutable
	private static Map<EntityPose, EntityDimensions> POSE_DIMENSIONS;

	@Unique
	EntityPose prevPose;
	
	@Unique
	EntityPose prevTickPose;

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	public void onInitDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
		builder.add(Crawl.Shared.CRAWL_REQUEST, false);
	}
	
	@WrapOperation(
		method = "updatePose",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/entity/player/PlayerEntity.setPose(Lnet/minecraft/entity/EntityPose;)V"
		)
	)
	public void onPreSetPose(PlayerEntity instance, EntityPose pose, Operation<Void> original) {
		if (!this.isSpectator() && !this.hasVehicle() && !this.abilities.flying) {
			boolean requested = getDataTracker().get(Shared.CRAWL_REQUEST);
			boolean swimming = isSwimming() || isTouchingWater();

			if (requested) {
				pose = swimming ? EntityPose.SWIMMING : Shared.CRAWLING;
			}
			else if (pose == EntityPose.SWIMMING && !swimming) {
				pose = Shared.CRAWLING;
			}
		}

		original.call(instance, pose);
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void onPoseMapCreation(CallbackInfo ci) {
		POSE_DIMENSIONS = ImmutableMap.<EntityPose, EntityDimensions>builder()
			.putAll(POSE_DIMENSIONS)
			.put(Crawl.Shared.CRAWLING, Crawl.Shared.CRAWLING_DIMENSIONS)
			.build();
	}
	
	@Inject(method = "tick", at = @At(value = "TAIL"))
	public void onTickEnd(CallbackInfo ci) {
		if (getPose() != prevTickPose) {
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