package ru.fewizz.crawl.mixin.client;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static net.minecraft.util.math.MathHelper.lerp;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import ru.fewizz.crawl.mixininterface.CrawlingState;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends BipedEntityRenderState> extends EntityModel<T> {

	@Shadow
	public ModelPart head;
	@Shadow
	public ModelPart hat;
	@Shadow
	public ModelPart body;
	@Shadow
	public ModelPart rightArm;
	@Shadow
	public ModelPart leftArm;
	@Shadow
	public ModelPart rightLeg;
	@Shadow
	public ModelPart leftLeg;

	BipedEntityModelMixin() { super(null);}

	// Prevent model change when in swimming pose but not in water
	@ModifyExpressionValue(
		method="setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;leaningPitch:F"
		)
	)
	float skipSwimmingRenderingIfNotInWater(float leaningPitch, BipedEntityRenderState state) {
		return state.isSwimming ? leaningPitch : 0.0F;
	}

	@Inject(
		method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V",
		at = @At("HEAD")
	)
	void beforeSetAngles(BipedEntityRenderState state, CallbackInfo ci) {
		head.setPivot(0.0F, 0.0F, 0.0F);
		head.roll = 0.0F;

		body.roll = 0.0F;
		body.pitch = 0.0F;
		body.pivotZ = 0.0F;

		leftLeg.pivotX = 1.9F;
		rightLeg.pivotX = -1.9F;

		leftArm.setPivot(5.0F, 2.0F, 0.0F);
		rightArm.setPivot(-5.0F, 2.0F, 0.0F);
	}

	@Unique
	private float l(float leaningPitch, float original, float changed) {
		return lerp(leaningPitch, original, changed);
	}

	@Unique
	private float la(float leaningPitch, float original, float changed) {
		return MathHelper.lerpAngleRadians(leaningPitch, original, changed);
	}

	@Unique
	private void llPivot(float lp, ModelPart mp, float x, float y, float z) {
		mp.setPivot(l(lp, mp.pivotX, x), l(lp, mp.pivotY, y), l(lp, mp.pivotZ, z));
	}

	@Unique
	private void llAngles(float lp, ModelPart mp, float roll, float yaw, float pitch) {
		mp.roll = la(lp, mp.roll, roll);
		mp.yaw = la(lp, mp.yaw, yaw);
		mp.pitch = la(lp, mp.pitch, pitch);
	}

	@Inject(
		method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V",
		at = @At("TAIL")
	)
	void afterSetAngles(BipedEntityRenderState state, CallbackInfo ci) {
		if (!((CrawlingState) state).isCrawling()) return;

		float limbFreq = state.limbFrequency;
		float lp = state.leaningPitch;
		float torsoRollDiv = 6F;
		float torsoPitchAngle = 0;
		float torsoYawAngle = (float) sin(limbFreq) / 5.0F;
		float torsoHeight = 12.0F;

		llPivot(
			lp, leftLeg,
			1.9F + ((float) sin(limbFreq) / torsoRollDiv) * torsoHeight, 12.0F + (float) magicF0(limbFreq - (3.0F/4.0F)*PI) * 2.0F, magicF0(limbFreq - PI/2.0)
		);
		llAngles(
			lp, leftLeg,
			-magicF1(limbFreq + PI) / 6.0F, torsoYawAngle, 0.0F
		);

		llPivot(
			lp, rightLeg,
			-1.9F + ((float) sin(limbFreq) / torsoRollDiv) * torsoHeight, 12.0F + (float) magicF0(limbFreq + PI/4.0F) * 2.0F, magicF0(limbFreq + PI/2.0)
		);
		llAngles(
			lp, rightLeg,
			magicF1(limbFreq) / 6.0F, torsoYawAngle, 0.0F
		);

		float torsoPivotY = torsoHeight - (float) cos(la(lp, body.pitch, torsoPitchAngle))*torsoHeight;
		float torsoPivotZ = (float) -sin(la(lp, body.pitch, torsoPitchAngle))*torsoHeight;

		llAngles(
			lp, body,
			(float) -sin(limbFreq) / torsoRollDiv, torsoYawAngle, torsoPitchAngle
		);

		body.pivotZ = torsoPivotZ;
		body.pivotY = torsoPivotY;

		llAngles(lp, head, -head.yaw, 0.0F, (float) (head.pitch - PI/2.0));

		head.pivotZ = l(lp, 0.0F, torsoPivotZ + (float) cos(limbFreq*2)/2.0F);
		head.pivotY = torsoPivotY;

		hat.copyTransform(head);

		llPivot(lp, leftArm, 5.0F, torsoPivotY + 2.0F, torsoPivotZ);
		llPivot(lp, rightArm, -5.0F, torsoPivotY + 2.0F, torsoPivotZ);

		if ((state.isUsingItem || state.handSwingProgress > 0) && state.activeHand == Hand.OFF_HAND) {
			llAngles(
				lp, leftArm,
				-leftArm.yaw, 0.0F, (float) (leftArm.pitch - PI/2.0)
			);
		}
		else {
			llAngles(
				lp, leftArm,
				(float)(-PI/2.0) + magicF0(limbFreq + PI/2.0), body.yaw - (float)(PI/2.0), -0.5F
			);
		}

		if ((state.isUsingItem || state.handSwingProgress > 0) && state.activeHand == Hand.MAIN_HAND) {
			llAngles(
				lp, rightArm,
				-rightArm.yaw, 0.0F, (float) (rightArm.pitch - PI/2.0)
			);
		}
		else {
			llAngles(
				lp, rightArm,
				(float)(PI/2.0) + -magicF0(limbFreq - PI/2.0), body.yaw + (float)(PI/2.0), -0.5F
			);
		}

	}

	@Unique
	private static float magicF0(double rad) {
		rad = rad % (PI * 2.0);
		if (rad <= PI / 2.0) {
			return (float) cos(rad*2.0);
		}

		return (float) (
			-cos((rad - PI / 2.0) * (2.0 / 3.0))
		);
	}

	@Unique
	private static float magicF1(double rad) {
		return (float) pow(sin(rad) + 1.0F, 2.0F);
	}

}