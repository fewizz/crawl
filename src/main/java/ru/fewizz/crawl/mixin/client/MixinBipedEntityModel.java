package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import ru.fewizz.crawl.CrawlMod;

@Mixin(BipedEntityModel.class)
public abstract class MixinBipedEntityModel<T extends LivingEntity> extends EntityModel<T> {
	
	@SuppressWarnings("unchecked")
	@Inject(
		method="setAngles",
		at=@At(
			value="RETURN"
		)
	)
	void postSetAngles(LivingEntity e, float f, float g, float h, float i, float j, CallbackInfo ci) {
		CrawlMod.Client.postTransformModel((BipedEntityModel<T>)(Object)this, e, f);
	}
}