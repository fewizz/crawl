package ru.fewizz.crawl.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;

import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.PlayerExtended;
import ru.fewizz.crawl.Crawl.Shared;

@Mixin(Player.class)
public abstract class PlayerMixin extends Avatar implements PlayerExtended {

	PlayerMixin() { super(null, null); }

	@Shadow @Final private Abilities abilities;

	@Unique Pose crawl_prevPose;
	@Unique Pose crawl_prevTickPose;
	@Unique boolean crawl_requested;

	@Override
	public boolean crawl_wasPreviouslyCrawling() {
		return (
			this.crawl_prevPose == Crawl.Shared.CRAWLING ||
			this.crawl_prevTickPose == Crawl.Shared.CRAWLING
		);
	}

	@Override
	public boolean crawl_getRequestedCrawling() {
		return this.crawl_requested;
	}

	@Override
	public void crawl_setRequestedCrawling(boolean value) {
		this.crawl_requested = value;
	}

	@ModifyArg(
		method = "updatePlayerPose",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/world/entity/player/Player.setPose(Lnet/minecraft/world/entity/Pose;)V"
		)
	)
	private Pose onPreUpdatePlayerPose(Pose pose) {
		if (!this.isSpectator() && !this.isPassenger() && !this.abilities.flying) {
			boolean swimming = this.isSwimming() || this.isInWater();

			if (this.crawl_requested) {
				pose = swimming ? Pose.SWIMMING : Shared.CRAWLING;
			}
			else if (pose == Pose.SWIMMING && !swimming) {
				pose = Shared.CRAWLING;
			}
		}

		return pose;
	}
	
	@Inject(method = "tick", at = @At(value = "TAIL"))
	public void onTickEnd(CallbackInfo ci) {
		var newPose = this.getPose();
		if (newPose != this.crawl_prevTickPose) {
			this.crawl_prevPose = this.crawl_prevTickPose;
		}
		this.crawl_prevTickPose = newPose;
	}

}