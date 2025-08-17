package ru.fewizz.crawl.compat.fpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import dev.tr7zw.firstperson.LogicHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.PlayerExtended;

// Can't use PlayerOffsetHandler/ActivationHandler,
// because default handler messes with crawling
@Mixin(value = LogicHandler.class, remap = false)
public class LogicHandlerMixin {

	@Shadow private Vec3 offset;

	@Unique
	private boolean isCrawling(Player player, float delta) {
		return player.getPose() == Crawl.Shared.CRAWLING || (
			player.getSwimAmount(0.0F) > 0 &&
			((PlayerExtended) player).crawl_getPrevPose() == Crawl.Shared.CRAWLING
		);
	}

	@ModifyReturnValue(
		method = "isCrawlingOrSwimming",
		at = @At(value = "RETURN"),
		remap = false
	)
	private boolean isCrawlingOrSwimming(boolean original, Player player) {
		return original || isCrawling(player, 0.0F);
	}

	@ModifyExpressionValue(
		method = "updatePositionOffset",
		at = @At(
			value = "INVOKE",
			target = "Ldev/tr7zw/firstperson/LogicHandler;isCrawlingOrSwimming("+
				"Lnet/minecraft/world/entity/player/Player;"+
			")Z"
		),
		remap = false
	)
	private boolean cancelDefaultCrawlOffset(boolean original, Entity entiy, float delta) {
		return original && !isCrawling((Player) entiy, delta);
	}

	@Inject(
		method = "updatePositionOffset",
		at = @At(value = "TAIL"),
		remap = false
	)
	private void applyCrawlOffset(Entity entiy, float delta, CallbackInfo ci) {
		if (entiy instanceof Player player && isCrawling(player, delta)) {
			double yaw = Mth.rotLerp(delta, player.yBodyRotO, player.yBodyRot);
			float a = ((Player) entiy).getSwimAmount(delta);
			float offset = (float) Mth.lerp(
				a,
				0.0F,
				0.4F + (Math.sin((a*Math.PI-Math.PI/4.0)*2.0) + 1.0)/2.0*0.5
			);
			this.offset = new Vec3(
				this.offset.x + offset * Math.sin(Math.toRadians(yaw)),
				this.offset.y,
				this.offset.z - offset * Math.cos(Math.toRadians(yaw))
			);
		}
	}

}
