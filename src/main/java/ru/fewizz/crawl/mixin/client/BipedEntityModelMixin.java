package ru.fewizz.crawl.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import ru.fewizz.crawl.CrawlingInfo;

import java.util.function.Consumer;

import static java.lang.Math.*;
import static net.minecraft.util.math.MathHelper.lerp;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends EntityModel<T> implements CrawlingInfo {
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

	@Shadow public float leaningPitch;

	@Shadow protected abstract float lerpAngle(float f, float g, float h);

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
		((CrawlingInfo)model).setCrawling(crawling);
	}

	// Prevent head pitch change when in swimming pose but not in water
	@Redirect(
		require = 1,
		method = "setAngles",
		at=@At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;lerpAngle(FFF)F",
			ordinal = 1
		)
	)
	float onLerp(BipedEntityModel bipedEntityModel, float f, float g, float h) {
		return h; // don't change pitch
	}

	// Prevent model change when in swimming pose but not in water
	@Redirect(
		require = 1,
		method="setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/entity/model/CrossbowPosing;method_29350(Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;F)V"
			)
		),
		at=@At(
			value="FIELD", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;leaningPitch:F"
		)
	)
	float skipSwimmingRenderingIfNotOnWater(BipedEntityModel<?> owner, T livingEntity, float f, float g, float h, float i, float j) {
		return livingEntity.isInSwimmingPose() ? leaningPitch : 0;
	}

	@Inject(
		require = 1,
		method="setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
		at=@At(
			value="HEAD"
		)
	)
	void beforeSetAngles(LivingEntity e, float dist, float _0, float _1, float headYawDegrees, float headPitchDegrees, CallbackInfo ci) {
		head.setPivot(0, 0, 0);
		head.roll = 0;

		torso.roll = 0;
		torso.pitch = 0;
		torso.pivotZ = 0;

		leftLeg.pivotX = 1.9F;
		rightLeg.pivotX = -1.9F;

		leftArm.setPivot(5, 2, 0);
		rightArm.setPivot(-5, 2, 0);
	}

	@Unique
	private float l(float original, float changed) {
		return lerp(leaningPitch, original, changed);
	}
	@Unique
	private float la(float original, float changed) {
		return lerpAngle(leaningPitch, original, changed);
	}

	@Unique
	private void llPivot(ModelPart mp, float x, float y, float z) {
		mp.setPivot(
			l(mp.pivotX, x),
			l(mp.pivotY, y),
			l(mp.pivotZ, z)
		);
	}

	@Unique
	private void llAngles(ModelPart mp, float roll, float yaw, float pitch) {
		mp.roll = la(mp.roll, roll);
		mp.yaw = la(mp.yaw, yaw);
		mp.pitch = la(mp.pitch, pitch);
	}

	@Inject(
		require = 1,
		method="setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
		at=@At(
			value="RETURN"
		)
	)
	void afterSetAngles(LivingEntity e, float dist, float _0, float _1, float headYawDegrees, float headPitchDegrees, CallbackInfo ci) {
		if(!crawling) return;

		MinecraftClient mc = MinecraftClient.getInstance();

		float torsoRollDiv = 6F;
		float torsoPitchAngle = 0;//- (float)(-cos(leaningPitch * 2*PI) + 1);
		float torsoYawAngle = (float) sin(dist) / 5F;
		float torsoHeight = 12F;

		llPivot(
			leftLeg,
			1.9F + ((float) sin(dist) / torsoRollDiv) * torsoHeight,
			12.0F + (float) magic0(dist - (3F/4F)*PI) * 2F,
			magic0(dist - PI/2)
		);
		llAngles(
			leftLeg,
			-magic1(dist + PI) / 6.F,
			torsoYawAngle,
			0
		);

		llPivot(
			rightLeg,
			-1.9F + ((float) sin(dist) / torsoRollDiv) * torsoHeight,
			12.0F + (float) magic0(dist + PI/4F) * 2F,
			magic0(dist + PI/2)
		);
		llAngles(
			rightLeg,
			magic1(dist) / 6.F,
			torsoYawAngle,
			0
		);

		float torsoPivotY = torsoHeight - (float)cos(la(torso.pitch, torsoPitchAngle))*torsoHeight;
		float torsoPivotZ = (float)-sin(la(torso.pitch, torsoPitchAngle))*torsoHeight;

		llAngles(
			torso,
			(float) -sin(dist) / torsoRollDiv,
			torsoYawAngle,
			torsoPitchAngle
		);

		torso.pivotZ = torsoPivotZ;
		torso.pivotY = torsoPivotY;

		llAngles(
			head,
			-head.yaw,
			0,
			(float) (head.pitch - PI / 2.0)
		);

		head.pivotZ = l(0, torsoPivotZ + (float) cos(dist*2)/2.0F);
		head.pivotY = torsoPivotY;

		helmet.copyPositionAndRotation(head);

		llPivot(
			leftArm,
			5,
			torsoPivotY + 2,
			torsoPivotZ
		);

		llPivot(
			rightArm,
			-5,
			torsoPivotY + 2,
			torsoPivotZ
		);

		Consumer<Hand> usingArmTransformer = (Hand hand) -> {
			if(hand == Hand.OFF_HAND)
				llAngles(
					leftArm,
					-leftArm.yaw,
					0,
					(float) (leftArm.pitch - PI / 2.0)
				);
			else
				llAngles(
					rightArm,
					-rightArm.yaw,
					0,
					(float) (rightArm.pitch - PI / 2.0)
				);
		};

		if(e.isUsingItem()) {
			usingArmTransformer.accept(Hand.MAIN_HAND);
			usingArmTransformer.accept(Hand.OFF_HAND);

			return;
		}

		if(handSwingProgress <= 0 || e.preferredHand != Hand.OFF_HAND) {
			llAngles(
				leftArm,
				(float)(-PI / 2.0) + magic0(dist + PI/2.0),
				torso.yaw - (float)(PI / 2.0),
				-0.5F
			);
		}
		else usingArmTransformer.accept(Hand.OFF_HAND);

		if(handSwingProgress <= 0 || e.preferredHand != Hand.MAIN_HAND) {
			llAngles(
				rightArm,
				(float)(PI / 2.0) + -magic0(dist - PI/2.0),
				torso.yaw + (float)(PI / 2.0),
				-0.5F
			);
		}
		else usingArmTransformer.accept(Hand.MAIN_HAND);

	}

	@Unique
	private static float magic1(double rad) {
		return (float) pow(sin(rad) + 1, 2);
	}

	// cos-like
	// http://yotx.ru/#!1/3_h/sH@1sH@0YM4X9t/2h/82z/bN@IIfyv7f/@/W/sgXd29w/2STTsxs4p4/F0i/G4dXmxu7@1v/n797@xsbd5AdkCbe/sgjd2ti92d/cP9kk07AbogPG4A9piPIIOdve39gEH
	@Unique
	private static float magic0(double rad) {
		rad = rad % (PI * 2.0);
		if(rad <= PI / 2.0)
			return (float) cos(rad*2.0);
		return (float)(
			-cos((rad - PI / 2.0) * (2.0 / 3.0))
		);
	}
}