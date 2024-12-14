package ru.fewizz.crawl.mixin.client;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import ru.fewizz.crawl.mixininterface.CrawlingState;

@Mixin(HumanoidModel.class)
public abstract class BipedEntityModelMixin<T extends HumanoidRenderState> extends EntityModel<T> {

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
		method="setupAnim",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;swimAmount:F"
		)
	)
	float skipSwimmingRenderingIfNotInWater(float leaningPitch, HumanoidRenderState state) {
		return state.isVisuallySwimming ? leaningPitch : 0.0F;
	}

	@Inject(
		method = "setupAnim",
		at = @At("HEAD")
	)
	void beforeSetAngles(HumanoidRenderState state, CallbackInfo ci) {
		head.setPos(0.0F, 0.0F, 0.0F);
		head.zRot = 0.0F;

		body.zRot = 0.0F;
		body.xRot = 0.0F;
		body.z = 0.0F;

		leftLeg.x = 1.9F;
		rightLeg.x = -1.9F;

		leftArm.setPos(5.0F, 2.0F, 0.0F);
		rightArm.setPos(-5.0F, 2.0F, 0.0F);
	}

	@Unique
	private float l(float leaningPitch, float original, float changed) {
		return Mth.lerp(leaningPitch, original, changed);
	}

	@Unique
	private float la(float leaningPitch, float original, float changed) {
		return Mth.rotLerpRad(leaningPitch, original, changed);
	}

	@Unique
	private void llPivot(float lp, ModelPart mp, float x, float y, float z) {
		mp.setPos(l(lp, mp.x, x), l(lp, mp.y, y), l(lp, mp.z, z));
	}

	@Unique
	private void llAngles(float lp, ModelPart mp, float roll, float yaw, float pitch) {
		mp.zRot = la(lp, mp.zRot, roll);
		mp.yRot = la(lp, mp.yRot, yaw);
		mp.xRot = la(lp, mp.xRot, pitch);
	}

	@Inject(
		method = "setupAnim",
		at = @At("TAIL")
	)
	void afterSetAngles(HumanoidRenderState state, CallbackInfo ci) {
		if (!((CrawlingState) state).isCrawling()) return;

		float limbFreq = state.walkAnimationPos;
		float lp = state.swimAmount;
		float torsoRollDiv = 6.0F;
		float torsoPitchAngle = 0.0F;
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

		float torsoPivotY = torsoHeight - (float) cos(la(lp, body.xRot, torsoPitchAngle))*torsoHeight;
		float torsoPivotZ = (float) -sin(la(lp, body.xRot, torsoPitchAngle))*torsoHeight;

		llAngles(
			lp, body,
			(float) -sin(limbFreq) / torsoRollDiv, torsoYawAngle, torsoPitchAngle
		);

		body.z = torsoPivotZ;
		body.y = torsoPivotY;

		llAngles(lp, head, -head.yRot, 0.0F, (float) (head.xRot - PI/2.0));

		head.z = l(lp, 0.0F, torsoPivotZ + (float) cos(limbFreq*2)/2.0F);
		head.y = torsoPivotY;

		hat.copyFrom(head);

		llPivot(lp, leftArm, 5.0F, torsoPivotY + 2.0F, torsoPivotZ);
		llPivot(lp, rightArm, -5.0F, torsoPivotY + 2.0F, torsoPivotZ);

		if ((state.isUsingItem || state.ticksUsingItem > 0) && state.useItemHand == InteractionHand.OFF_HAND) {
			llAngles(
				lp, leftArm,
				-leftArm.yRot, 0.0F, (float) (leftArm.xRot - PI/2.0)
			);
		}
		else {
			llAngles(
				lp, leftArm,
				(float)(-PI/2.0) + magicF0(limbFreq + PI/2.0), body.yRot - (float)(PI/2.0), -0.5F
			);
		}

		if ((state.isUsingItem || state.ticksUsingItem > 0) && state.useItemHand == InteractionHand.MAIN_HAND) {
			llAngles(
				lp, rightArm,
				-rightArm.yRot, 0.0F, (float) (rightArm.xRot - PI/2.0)
			);
		}
		else {
			llAngles(
				lp, rightArm,
				(float)(PI/2.0) + -magicF0(limbFreq - PI/2.0), body.yRot + (float)(PI/2.0), -0.5F
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