package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.entity.LivingEntity;
import ru.fewizz.crawl.CrawlMod;

@Mixin(ElytraEntityModel.class)
public class MixinElytraEntityModel {
	@Shadow
	@Final
	private ModelPart field_3364;
	
	@Shadow
	@Final
	private ModelPart field_3365;
	
	private ModelPart origCopy1;
	private ModelPart origCopy2;
	
	@Inject(
		method="<init>",
		at=@At(
			value="RETURN"
		)
	)
	void onContruct(CallbackInfo ci) {
		origCopy1 = field_3364.method_29991();
		origCopy2 = field_3365.method_29991();
	}
	
	@Inject(
		method="setAngles",
		at=@At(
			value="HEAD"
		),
		cancellable = true
	)
	void postSetAngles(LivingEntity e, float f, float g, float h, float i, float j, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		LivingEntityRenderer<?, ?> r =
				(LivingEntityRenderer<?, ?>) client.getEntityRenderDispatcher().getRenderer(e);

		if(e.getPose() != CrawlMod.Shared.CRAWLING
			|| !(r.getModel() instanceof BipedEntityModel)) {
			field_3364.copyPositionAndRotation(origCopy1);
			field_3365.copyPositionAndRotation(origCopy2);
			return;
		}
		
		BipedEntityModel<?> m = (BipedEntityModel<?>) r.getModel();
		field_3364.copyPositionAndRotation(m.torso);
		field_3365.copyPositionAndRotation(m.torso);
		ci.cancel();
	}
}
