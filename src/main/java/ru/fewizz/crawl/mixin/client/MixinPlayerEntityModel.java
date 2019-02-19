package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import ru.fewizz.crawl.CrawlMod;

@Mixin(PlayerEntityModel.class)
public abstract class MixinPlayerEntityModel<E extends LivingEntity> extends BipedEntityModel<E> {
	
	@SuppressWarnings("unchecked")
	@Inject(method="method_17087", at=@At("RETURN"))
	void postSetAngles(LivingEntity e, float f1, float f2, float f3, float f4, float f5, float f6, CallbackInfo ci) {
		CrawlMod.Client.postTransformModel((PlayerEntityModel<E>)(Object)this, e, f1);
	}
}