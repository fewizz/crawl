package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import ru.fewizz.crawl.CrawlMod;

@Mixin(BipedEntityModel.class)
public abstract class MixinBipedEntityModel<T extends LivingEntity> extends EntityModel<T> {
	
	@SuppressWarnings("unchecked")
	@Inject(
		method="method_17087",
		at=@At(
			value="RETURN"
		)
	)
	void postSetAngles(LivingEntity e, float f1, float f2, float f3, float f4, float f5, float f6, CallbackInfo ci) {
		if(e instanceof PlayerEntity)
			CrawlMod.Client.postTransformModel((BipedEntityModel<T>)(Object)this, e, f1);
	}
}