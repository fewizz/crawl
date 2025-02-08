package ru.fewizz.crawl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import ru.fewizz.crawl.Crawl;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@WrapMethod(method = "getBlockJumpFactor")
	float getBlockJumpFactor(Operation<Float> original) {
		float result = original.call();
		if (getPose() == Crawl.Shared.CRAWLING) {
			result /= 2.0F;
		}
		return result;
	}

	@Shadow
	abstract public Pose getPose();

	@ModifyReturnValue(method = "isVisuallyCrawling", at = @At("RETURN"))
	private boolean isVisuallyCrawling(boolean original) {
		return original || getPose() == Crawl.Shared.CRAWLING;
	}

	@ModifyReturnValue(method = "isSteppingCarefully", at = @At("RETURN"))
	private boolean isSteppingCarefully(boolean original) {
		return original || getPose() == Crawl.Shared.CRAWLING;
	}

	@ModifyReturnValue(method = "isDiscrete", at = @At("RETURN"))
	private boolean isDiscrete(boolean original) {
		return original || getPose() == Crawl.Shared.CRAWLING;
	}

}
