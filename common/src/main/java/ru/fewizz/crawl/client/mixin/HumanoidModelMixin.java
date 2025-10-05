package ru.fewizz.crawl.client.mixin;

import static net.minecraft.util.Mth.PI;
import static net.minecraft.util.Mth.cos;
import static net.minecraft.util.Mth.sin;

import org.spongepowered.asm.mixin.Final;
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
import net.minecraft.world.entity.HumanoidArm;
import ru.fewizz.crawl.client.CrawlClient;
import ru.fewizz.crawl.client.mixininterface.CrawlingState;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends HumanoidRenderState> extends EntityModel<T> {

    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;

	HumanoidModelMixin() { super(null);}

	// Prevent model change when in swimming pose but not in water
	@ModifyExpressionValue(
		method = "setupAnim*",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;swimAmount:F"
		)
	)
	float skipSwimmingRenderingIfNotInWater(float leaningPitch, HumanoidRenderState state) {
		return !CrawlClient.replaceAnimation || state.isVisuallySwimming ? leaningPitch : 0.0F;
	}

	@Inject(
		method = "setupAnim*",
		at = @At("TAIL")
	)
	void afterSetAngles(HumanoidRenderState state, CallbackInfo ci) {
		if (!(CrawlClient.replaceAnimation && ((CrawlingState) state).isCrawling())) return;

		float walkDist = state.walkAnimationPos;
		float sa = state.swimAmount;
		float bodyYRotFreq = 6.0F;
		float bodyXRot = 0.0F;
		float bodyYRot = sin(walkDist) / 5.0F;
		float zOffset = 0.0F;
		float yOffset = 0.0F;

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
			(state.isUsingItem && ((state.useItemHand == InteractionHand.OFF_HAND) == (state.mainArm == HumanoidArm.LEFT))) ||
			(state.attackTime > 0 && state.attackArm == HumanoidArm.LEFT)
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
			(state.isUsingItem && ((state.useItemHand == InteractionHand.MAIN_HAND) == (state.mainArm == HumanoidArm.RIGHT))) ||
			(state.attackTime > 0 && state.attackArm == HumanoidArm.RIGHT)
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