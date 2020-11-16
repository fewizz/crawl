package ru.fewizz.crawl.mixin.client;

import com.sun.org.apache.xpath.internal.operations.Mod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import ru.fewizz.crawl.Crawl.Shared;
import ru.fewizz.crawl.WasCrawlingPrevTick;

import static java.lang.Math.*;
import static net.minecraft.util.math.MathHelper.lerp;

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

	@Shadow public float leaningPitch;
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

	// Prevent head pitch change when in swimming pose but not in water
	@Redirect(
		require = 1,
		method = "setAngles",
		at=@At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;lerpAngle(FFF)F",
			ordinal = 2
		)
	)
	float onLerp(BipedEntityModel bipedEntityModel, float f, float g, float h) {
		return head.pitch; // don't change pitch
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

	//public ModelPart headCopy = ModelPart.method_29991();

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
		//torso.setPivot(0, 0, 0);

		leftLeg.pivotX = 1.9F;
		rightLeg.pivotX = -1.9F;

		leftArm.setPivot(5, 2, 0);
		rightArm.setPivot(-5, 2, 0);
	}

	@Inject(
			require = 1,
			method="setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
			at=@At(
				value="RETURN"
			)
	)
	void afterSetAngles(LivingEntity e, float dist, float _0, float _1, float headYawDegrees, float headPitchDegrees, CallbackInfo ci) {
		if(crawling)
			crawl$setCrawlModel(e, dist);
	}

	private float ll(float original, float changed) {
		return lerp(leaningPitch, original, changed);
	}

	private void llPivot(ModelPart mp, float x, float y, float z) {
		mp.setPivot(
			ll(mp.pivotX, x),
			ll(mp.pivotY, y),
			ll(mp.pivotZ, z)
		);
	}

	private void llAngles(ModelPart mp, float roll, float yaw, float pitch) {
		mp.roll = ll(mp.roll, roll);
		mp.yaw = ll(mp.yaw, yaw);
		mp.pitch = ll(mp.pitch, pitch);
	}

	private void crawl$setCrawlModel(LivingEntity e, float dist) {
		MinecraftClient mc = MinecraftClient.getInstance();

		head.roll = ll(head.roll, -head.yaw);
		head.yaw = ll(head.yaw, 0);
		head.pitch = ll(head.pitch, (float) (head.pitch - PI / 2.0));

		head.pivotZ = ll(head.pivotZ, (float) (cos(dist*2) + 1)/2.0F);

		float torsoRollDiv = 6F;

		torso.yaw = ll(torso.yaw, (float) sin(dist) / 5F);
		torso.roll = ll(torso.roll, (float) -sin(dist) / torsoRollDiv);

		llPivot(
			leftLeg,
			1.9F + ((float) sin(dist) / torsoRollDiv) * 12,
			12.0F + (float) sin(dist) * 1.5F,
			0.0F
		);
		llAngles(
			leftLeg,
			-magic1(dist + PI) / 8.F,
			torso.yaw,
			0
		);

		llPivot(
			rightLeg,
			-1.9F + ((float) sin(dist) / torsoRollDiv) * 12,
			12.0F + (float) sin(dist + PI) * 1.5F,
			0.0F
		);
		llAngles(
			rightLeg,
			magic1(dist) / 8.F,
			torso.yaw,
			0
		);

		llPivot(
			leftArm,
			5,
			2,
			0
		);

		llPivot(
			rightArm,
			-5,
			2,
			0
		);

		if(e.isUsingItem()) {
			leftArm.pitch = ll(leftArm.pitch, leftArm.pitch - (float)PI / 2.0F);
			rightArm.pitch = ll(rightArm.pitch, rightArm.pitch - (float)PI / 2.0F);
			return;
		}

		if(handSwingProgress <= 0 || e.preferredHand != Hand.OFF_HAND) {
			llAngles(
				leftArm,
				(float)(-PI / 2.0) + magic0(dist + PI/2.0),
				torso.yaw - (float)(PI / 2.0),
				0
			);
		}
		else {
			leftArm.pitch = ll(leftArm.pitch, leftArm.pitch - (float)PI / 2.0F);
		}

		if(handSwingProgress <= 0 || e.preferredHand != Hand.MAIN_HAND) {
			llAngles(
				rightArm,
				(float)(PI / 2.0) + -magic0(dist - PI/2.0),
				torso.yaw + (float)(PI / 2.0),
				0
			);
		}
		else {
			rightArm.pitch = ll(rightArm.pitch, rightArm.pitch - (float)PI / 2.0F);
		}

	}

	private static float magic1(double rad) {
		return (float) pow(sin(rad) + 1, 2);
	}

	private static float magic0(double rad) {
		rad = rad % (PI * 2.0);
		if(rad <= PI / 2.0)
			return (float) cos(rad*2.0);
		return (float)(
			-cos((rad - PI / 2.0) * (2.0 / 3.0))
		);
	}
}