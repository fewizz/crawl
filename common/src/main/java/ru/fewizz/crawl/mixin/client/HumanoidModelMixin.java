package ru.fewizz.crawl.mixin.client;

import static net.minecraft.util.Mth.PI;
import static net.minecraft.util.Mth.sin;
import static net.minecraft.util.Mth.cos;

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
public abstract class HumanoidModelMixin<T extends HumanoidRenderState> extends EntityModel<T> {

	@Shadow
	public ModelPart head;
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

	HumanoidModelMixin() { super(null);}

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
	private void lPos(float lp, ModelPart mp, float x, float y, float z) {
		mp.setPos(l(lp, mp.x, x), l(lp, mp.y, y), l(lp, mp.z, z));
	}

	@Unique
	private void lRot(float lp, ModelPart mp, float roll, float yaw, float pitch) {
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

		float walkDist = state.walkAnimationPos;
		float sa = state.swimAmount;
		float bodyYRotFreq = 6.0F;
		float bodyXRot = 0.0F;
		float bodyYRot = sin(walkDist) / 5.0F;
		float bodyHeight = 12.0F;

		lPos(
			sa, leftLeg,
			1.9F + sin(walkDist) / bodyYRotFreq * bodyHeight,
			12.0F + magicF0(walkDist - 3.0F/4.0F*PI) * 2.0F,
			magicF0(walkDist - PI/2.0F)
		);
		lRot(sa, leftLeg, -magicF1(walkDist + PI) / 6.0F, bodyYRot, 0.0F);

		lPos(
			sa, rightLeg,
			-1.9F + sin(walkDist) / bodyYRotFreq * bodyHeight,
			12.0F + magicF0(walkDist + PI/4.0F) * 2.0F,
			magicF0(walkDist + PI/2.0F)
		);
		lRot(sa, rightLeg, magicF1(walkDist) / 6.0F, bodyYRot, 0.0F);

		float torsoPosY = (1.0F - cos(la(sa, body.xRot, bodyXRot)))*bodyHeight;
		float torsoPosZ = -sin(la(sa, body.xRot, bodyXRot))*bodyHeight;

		lRot(sa, body, -sin(walkDist) / bodyYRotFreq, bodyYRot, bodyXRot);

		body.z = torsoPosZ;
		body.y = torsoPosY;

		lRot(sa, head, -head.yRot, 0.0F, head.xRot - PI/2.0F);

		head.z = l(sa, 0.0F, torsoPosZ + cos(walkDist*2.0F)/2.0F);
		head.y = torsoPosY;

		lPos(sa, leftArm, 5.0F, torsoPosY + 2.0F, torsoPosZ);
		lPos(sa, rightArm, -5.0F, torsoPosY + 2.0F, torsoPosZ);

		if ((state.isUsingItem || state.ticksUsingItem > 0) && state.useItemHand == InteractionHand.OFF_HAND) {
			lRot(
				sa, leftArm,
				-leftArm.yRot, 0.0F, leftArm.xRot - PI/2.0F
			);
		}
		else {
			lRot(
				sa, leftArm,
				-PI/2.0F + magicF0(walkDist + PI/2.0F), body.yRot - PI/2.0F, -0.5F
			);
		}

		if ((state.isUsingItem || state.ticksUsingItem > 0) && state.useItemHand == InteractionHand.MAIN_HAND) {
			lRot(
				sa, rightArm,
				-rightArm.yRot, 0.0F, rightArm.xRot - PI/2.0F
			);
		}
		else {
			lRot(
				sa, rightArm,
				PI/2.0F + -magicF0(walkDist - PI/2.0F), body.yRot + PI/2.0F, -0.5F
			);
		}

	}

	@Unique
	private static float magicF0(float rad) {
		rad = rad % (PI * 2.0F);
		if (rad <= PI / 2.0F) {
			return cos(rad*2.0F);
		}

		return -cos((rad - PI / 2.0F) * (2.0F / 3.0F));
	}

	@Unique
	private static float magicF1(float rad) {
		float r = sin(rad) + 1.0F;
		return r*r;
	}

}