package ru.fewizz.crawl.client.mixin;

import static net.minecraft.util.Mth.PI;
import static net.minecraft.util.Mth.cos;
import static net.minecraft.util.Mth.sin;

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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import ru.fewizz.crawl.client.CrawlClient;
import ru.fewizz.crawl.client.mixininterface.CrawlingState;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends EntityModel<T> implements CrawlingState {

	@Shadow public ModelPart head;
	@Shadow public ModelPart body;
	@Shadow public ModelPart rightArm;
	@Shadow public ModelPart leftArm;
	@Shadow public ModelPart rightLeg;
	@Shadow public ModelPart leftLeg;

	@Shadow public float swimAmount;
	@Shadow private HumanoidArm getAttackArm(LivingEntity livingEntity) { return null; }

	@Unique
	private boolean crawling = false;
	@Override public boolean isCrawling() { return this.crawling; }
	@Override public void setCrawling(boolean v) { this.crawling = v; }

	HumanoidModelMixin() { super(null);}

	// Prevent model change when in swimming pose but not in water
	@ModifyExpressionValue(
		method = "setupAnim",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/model/HumanoidModel;swimAmount:F",
			ordinal = 0
		)
	)
	float skipSwimmingRenderingIfNotInWater(float leaningPitch, LivingEntity state) {
		return !CrawlClient.replaceAnimation || state.isVisuallySwimming() ? leaningPitch : 0.0F;
	}

	@Inject(
		method = "setupAnim",
		at = @At("TAIL")
	)
	void afterSetAngles(LivingEntity state, float walkDist, float f_0, float f_1, float f_2, float f_3, CallbackInfo ci) {
		if (!(CrawlClient.replaceAnimation && this.crawling)) return;

		float sa = this.swimAmount;
		float bodyYRotFreq = 6.0F;
		float bodyXRot = 0.0F;
		float bodyYRot = sin(walkDist) / 5.0F;
		float zOffset = 0.0F;
		float yOffset = 0.0F;

		head.setPos(0.0F, 0.0F, 0.0F);
		head.zRot = 0;

		body.setRotation(0.0F, 0.0F, 0.0F);

		leftLeg.x = 1.9F;
		rightLeg.x = -1.9F;

		leftArm.setPos(5.0F, 2.0F, 0.0F);
		rightArm.setPos(-5.0F, 2.0F, 0.0F);

		lPos(
			sa, leftLeg,
			leftLeg.x + sin(walkDist) / bodyYRotFreq * leftLeg.y,
			leftLeg.y + magicF0(walkDist - 3.0F/4.0F*PI) * 2.0F - 1.0F + yOffset,
			magicF0(walkDist - PI/2.0F) + zOffset
		);
		lRot(sa, leftLeg, -magicF1(walkDist + PI) / 6.0F, bodyYRot, 0.0F);

		lPos(
			sa, rightLeg,
			rightLeg.x + sin(walkDist) / bodyYRotFreq * rightLeg.y,
			rightLeg.y + magicF0(walkDist + PI/4.0F) * 2.0F - 1.0F + yOffset,
			magicF0(walkDist + PI/2.0F) + zOffset
		);
		lRot(sa, rightLeg, magicF1(walkDist) / 6.0F, bodyYRot, 0.0F);

		float bodyHeight = 12.0F;
		lPos(
			sa, body,
			body.x,
			(1.0F - cos(la(sa, body.xRot, bodyXRot)))*bodyHeight + yOffset,
			-sin(la(sa, body.xRot, bodyXRot))*bodyHeight + zOffset
		);
		lRot(sa, body, -sin(walkDist) / bodyYRotFreq, bodyYRot, bodyXRot);

		lPos(sa, head, head.x, head.y+yOffset, head.z+cos(walkDist*2.0F)/2.0F+zOffset);
		lRot(sa, head, -head.yRot, 0.0F, head.xRot - PI/2.0F);

		leftArm.y = body.y + 2.0F; leftArm.z = head.z;
		rightArm.y = body.y + 2.0F; rightArm.z = head.z;

		if (
			(state.isUsingItem() && ((state.getUsedItemHand() == InteractionHand.OFF_HAND) == (state.getMainArm() == HumanoidArm.LEFT))) ||
			(this.attackTime > 0 && this.getAttackArm(state) == HumanoidArm.LEFT)
		) {
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

		if (
			(state.isUsingItem() && ((state.getUsedItemHand() == InteractionHand.MAIN_HAND) == (state.getMainArm() == HumanoidArm.RIGHT))) ||
			(this.attackTime > 0 && this.getAttackArm(state) == HumanoidArm.RIGHT)
		) {
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
	private float l(float leaningPitch, float original, float changed) {
		return Mth.lerp(leaningPitch, original, changed);
	}

	@Unique
	private float la(float leaningPitch, float original, float changed) {
		return Mth.rotLerp(leaningPitch, original*Mth.RAD_TO_DEG, changed*Mth.RAD_TO_DEG)*Mth.DEG_TO_RAD;
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