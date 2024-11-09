package ru.fewizz.crawl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import ru.fewizz.crawl.Crawl;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@WrapMethod(method = "getJumpVelocityMultiplier")
	float onGetJumpVelocityMultiplierReturn(Operation<Float> original) {
		float result = original.call();
		if (getPose() == Crawl.Shared.CRAWLING) {
			result /= 2.0F;
		}
		return result;
	}

	@Shadow
	abstract public EntityPose getPose();

	@WrapMethod(method = "isCrawling")
	public boolean isCrawling(Operation<Boolean> original) {
		return original.call() || getPose() == Crawl.Shared.CRAWLING;
	}

}
