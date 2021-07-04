package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.entity.LivingEntity;
import ru.fewizz.crawl.Crawl;

@Mixin(ElytraEntityModel.class)
public class ElytraEntityModelMixin {
	@Shadow
	@Final
	private ModelPart leftWing;
	
	@Shadow
	@Final
	private ModelPart rightWing;

	@Unique
	private ModelPart leftCopy;
	@Unique
	private ModelPart rightCopy;
	
	@Inject(
		require = 1,
		method="<init>",
		at=@At(
			value="RETURN"
		)
	)
	void onConstruct(CallbackInfo ci) {
		leftCopy = new ModelPart(null, null);
		leftCopy.copyTransform(leftWing);
		rightCopy = new ModelPart(null, null);
		rightCopy.copyTransform(rightWing);
	}
	
	@Inject(
		require = 1,
		method="setAngles",
		at=@At(
			value="HEAD"
		),
		cancellable = true
	)
	void preSetAngles(LivingEntity e, float f, float g, float h, float i, float j, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		LivingEntityRenderer<?, ?> r =
				(LivingEntityRenderer<?, ?>) client.getEntityRenderDispatcher().getRenderer(e);

		if(e.getPose() != Crawl.Shared.CRAWLING || !(r.getModel() instanceof BipedEntityModel)) {
			leftWing.copyTransform(leftCopy);
			rightWing.copyTransform(rightCopy);
			return;
		}

		Object m0 = r.getModel();
		if(!(m0 instanceof BipedEntityModel<?>)) return;

		BipedEntityModel<?> m = (BipedEntityModel<?>) m0;
		leftWing.copyTransform(m.body);
		rightWing.copyTransform(m.body);
		ci.cancel();
	}
}
