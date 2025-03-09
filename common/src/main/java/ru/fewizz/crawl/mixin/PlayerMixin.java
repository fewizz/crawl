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

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.Crawl.Shared;
import ru.fewizz.crawl.mixininterface.PrevPoseState;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PrevPoseState {

	PlayerMixin() { super(null, null); }

	@Shadow @Final private Abilities abilities;
	@Shadow @Final @Mutable private static Map<Pose, EntityDimensions> POSES;

	@Unique Pose prevPose;
	@Unique Pose prevTickPose;

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void onDefineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
		builder.define(Crawl.Shared.CRAWL_REQUEST, false);
	}
	
	@WrapOperation(
		method = "updatePlayerPose",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/world/entity/player/Player.setPose(Lnet/minecraft/world/entity/Pose;)V"
		)
	)
	private void onPreUpdatePlayerPose(Player instance, Pose pose, Operation<Void> original) {
		if (!this.isSpectator() && !this.isPassenger() && !this.abilities.flying) {
			boolean requested = getEntityData().get(Shared.CRAWL_REQUEST);
			boolean swimming = isSwimming() || isInWater();

			if (requested) {
				pose = swimming ? Pose.SWIMMING : Shared.CRAWLING;
			}
			else if (pose == Pose.SWIMMING && !swimming) {
				pose = Shared.CRAWLING;
			}
		}

		original.call(instance, pose);
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void onPoseMapCreation(CallbackInfo ci) {
		POSES = ImmutableMap.<Pose, EntityDimensions>builder()
			.putAll(POSES)
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
	public Pose getPrevPose() {
		return prevPose;
	}
	
	@Override
	public Pose getPrevTickPose() {
		return prevTickPose;
	}

}