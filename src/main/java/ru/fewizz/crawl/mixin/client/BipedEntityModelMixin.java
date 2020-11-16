package ru.fewizz.crawl.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import ru.fewizz.crawl.Crawl.Shared;
import ru.fewizz.crawl.WasCrawlingPrevTick;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends EntityModel<T> implements WasCrawlingPrevTick {
	@Shadow
	public ModelPart head;
	@Shadow
	public ModelPart helmet;
	@Shadow
	public ModelPart torso;
	@Shadow
	public ModelPart rightArm;
	@Shadow
	public ModelPart leftArm;
	@Shadow
	public ModelPart rightLeg;
	@Shadow
	public ModelPart leftLeg;
	@Shadow
	public BipedEntityModel.ArmPose leftArmPose;
	@Shadow
	public BipedEntityModel.ArmPose rightArmPose;

	public boolean crawling = false;

	@Override
	public void setCrawling(boolean crawling) {
		this.crawling = crawling;
	}
	@Override
	public boolean isCrawling() {
		return crawling;
	}

	@Inject(
			require = 1,
			method="setAttributes",
			at=@At(
					value="RETURN"
			)
	)
	void onSetAttributes(BipedEntityModel<T> model, CallbackInfo ci) {
		((WasCrawlingPrevTick)model).setCrawling(crawling);
	}

	@Redirect(
			require = 1,
			method = "animateModel(Lnet/minecraft/entity/LivingEntity;FFF)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/LivingEntity;getLeaningPitch(F)F"
			)
	)
	float onAnimateModelGetLeaningPitch(T p, float delta) {
		return p.getPose() == Shared.CRAWLING ? 0 : p.getLeaningPitch(delta);
	}

	@Inject(
		require = 1,
		method="setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
		at=@At(
			value="RETURN"
		)
	)
	void onSetAnglesReturn(LivingEntity e, float dist, float _0, float _1, float _2, float _3, CallbackInfo ci) {
		if(!(e instanceof AbstractClientPlayerEntity)) return;
		MinecraftClient mc = MinecraftClient.getInstance();

		float pitchMul = e.getLeaningPitch(mc.getTickDelta());

		//int crawledTicksBack = ((LastTimeCrawledInfo) e).crawledTicksBack();
		//((LastTimeCrawledInfo) e).setCrawledTicksBack(crawling ? 0 : crawledTicksBack + 1);



		if((pitchMul == 0  && !crawling) || e.isInSwimmingPose()) {
			tryRestore();
			return;
		}

		float yOffset = 20;
		float as = 1F;

		head.setPivot(0, yOffset*pitchMul, -6*pitchMul);
		helmet.setPivot(0, yOffset*pitchMul, -6*pitchMul);

		torso.setPivot(0, yOffset*pitchMul, -6*pitchMul);
		torso.pitch = (float) (Math.PI / 2)*pitchMul;
		torso.yaw = (float) -(Math.sin(dist * as))*pitchMul / 10F;
		torso.roll = (float) -(Math.sin(dist * as))*pitchMul / 5F;

		leftLeg.setPivot(
			1.9F + -(float) Math.sin(dist * as)*pitchMul,
			MathHelper.lerp(pitchMul, 12, yOffset + 0.2F),
			(6.5F + (float) -(Math.sin(dist * as) + 1)*2)*pitchMul
		);
		leftLeg.pitch = (float) (Math.PI / 2)*pitchMul;
		leftLeg.yaw = MathHelper.lerp(pitchMul, leftLeg.yaw, (float) (Math.cos(dist * as) + .7F) / 3F);

		rightLeg.setPivot(
			-1.9F + -(float) Math.sin(dist * as)*pitchMul,
			MathHelper.lerp(pitchMul, 12, yOffset + 0.2F),
			(6.5F + (float) -(Math.cos(dist * as) + 1)*2)*pitchMul
		);
		rightLeg.pitch = (float) (Math.PI / 2)*pitchMul;
		rightLeg.yaw = MathHelper.lerp(pitchMul, rightLeg.yaw, (float) (Math.sin(dist * as) - .7F) / 3F);

		float xArmOffset = 4.2F;
		leftArm.setPivot(
			5 - (5 - xArmOffset)*pitchMul,
			2 + yOffset*pitchMul,
			(-6 + (float) Math.cos(dist*as)*2)*pitchMul
		);

		rightArm.setPivot(
			-(5 - (5 - xArmOffset)*pitchMul),
			2 + yOffset*pitchMul,
			(-6 + (float) Math.sin(dist*as)*2)*pitchMul
		);

		if(e.isUsingItem()) {
			return;
		}

		if(handSwingProgress <= 0 || e.preferredHand != Hand.OFF_HAND) {
			leftArm.roll = MathHelper.lerp(pitchMul, leftArm.roll, (float) (-Math.PI / 2));
			leftArm.yaw = 0;
			leftArm.pitch = MathHelper.lerp(pitchMul, leftArm.pitch, -1.3F + magic(dist * as + Math.PI / 2.0));
		}
		if(handSwingProgress <= 0 || e.preferredHand != Hand.MAIN_HAND) {
			rightArm.roll = MathHelper.lerp(pitchMul, rightArm.roll, (float) (Math.PI / 2));
			rightArm.yaw = 0;
			rightArm.pitch = MathHelper.lerp(pitchMul, rightArm.pitch, -1.3F + magic(dist * as - Math.PI / 2.0));
		}
	}

	private static float magic(double rad) {
		rad = rad % (Math.PI * 2);
		if(rad <= Math.PI / 2.0)
			return (float) Math.cos(rad*2.0);
		return (float)(
			-Math.cos((rad - Math.PI / 2.0) * (2.0 / 3.0))
		);
	}

	void tryRestore() {
		head.setPivot(0, 0, 0);
		head.roll = 0;
		helmet.setPivot(0, 0, 0);

		torso.roll = 0;
		torso.setPivot(0, 0, 0);

		leftLeg.pivotX = 1.9F;
		rightLeg.pivotX = -1.9F;

		leftArm.setPivot(5, 2, 0);
		rightArm.setPivot(-5, 2, 0);
	}
}