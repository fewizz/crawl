package ru.fewizz.crawl.compat.fpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tr7zw.firstperson.LogicHandler;
import net.minecraft.world.entity.player.Player;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.mixininterface.PrevPoseState;

@Mixin(value = LogicHandler.class, remap = false)
public class LogicHandlerMixin {

	@ModifyReturnValue(
		method = "isCrawlingOrSwimming",
		at = @At(value = "RETURN"),
		remap = false
	)
	private boolean isCrawlingOrSwimming(
		boolean original,
		@Local(argsOnly = true) Player player
	) {
		return original ||
			((PrevPoseState) player).getPrevPose() == Crawl.Shared.CRAWLING ||
			((PrevPoseState) player).getPrevTickPose() == Crawl.Shared.CRAWLING;
	}

}
