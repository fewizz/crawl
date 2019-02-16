package ru.fewizz.crawl.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Mouse;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.fewizz.crawl.CrawlMod;
import ru.fewizz.crawl.CrawlMod.Client;
import ru.fewizz.crawl.CrawlMod.Shared;


public class Mixins {
	@Mixin(PlayerEntity.class)
	public static abstract class MixinPlayerEntity extends Entity {

		public MixinPlayerEntity(EntityType<?> entityType_1, World world_1) {
			super(entityType_1, world_1);
		}

		@Inject(method="initDataTracker", at=@At("HEAD"))
		public void onInitDataDtracker(CallbackInfo ci) {
			getDataTracker().startTracking(CrawlMod.Shared.IS_CRAWLING, false);
		}
		
		@Inject(method="updateSize", at=@At("RETURN"))
		public void onSetSize(CallbackInfo ci) {
			if(Shared.isPlayerCrawling((PlayerEntity)(Object)this)) {
				setSize(0.6F, 0.6F);
			}
		}
	}
	
	@Mixin(ClientPlayerEntity.class)
	static abstract class MixinClientPlayerEntity extends PlayerEntity {

		public MixinClientPlayerEntity(World world_1, GameProfile gameProfile_1) {
			super(world_1, gameProfile_1);
		}
		
		// Ohh.. Why?
		@Overwrite
		private boolean method_3150(BlockPos blockPos_1) {
			if (this.isSwimming() || CrawlMod.Shared.isPlayerCrawling(this)) {
				return !this.method_7326(blockPos_1);
	      	} else {
    	  		return !this.method_7352(blockPos_1);
	      	}
		}
		
		@Inject(method="updateMovement", at=@At(value="INVOKE", target="net/minecraft/client/network/AbstractClientPlayerEntity.updateMovement()V"))
		public void beforeSuperMovementUpdate(CallbackInfo ci) {
			Client.logic((ClientPlayerEntity)(Object)this);
		}
	}
	
	@Mixin(BipedEntityModel.class)
	public static class MixinBipedEntityModel<E extends LivingEntity> {
		
		@SuppressWarnings("unchecked")
		@Inject(method="method_17087", at=@At("RETURN"))
		void postSetAngles(LivingEntity e, float f1, float f2, float f3, float f4, float f5, float f6, CallbackInfo ci) {
			CrawlMod.Client.postTransformModel((BipedEntityModel<E>)(Object)this, e, f1);
		}
	}
	
	@Mixin(PlayerEntityRenderer.class)
	static abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

		public MixinPlayerEntityRenderer(EntityRenderDispatcher entityRenderDispatcher_1,
				PlayerEntityModel<AbstractClientPlayerEntity> entityModel_1, float float_1) {
			super(entityRenderDispatcher_1, entityModel_1, float_1);
		}

		@Inject(method="method_4220", at=@At("HEAD"))
		public void onRightArmRender(CallbackInfo ci) {
			CrawlMod.Client.transformArms = false;
			Client.tryRestorePlayerModel(this.getModel());
		}
		
		@Inject(method="method_4221", at=@At("HEAD"))
		public void onLeftArmRender(CallbackInfo ci) {
			CrawlMod.Client.transformArms = false;
			Client.tryRestorePlayerModel(this.getModel());
		}
	}
	
	@Mixin(Mouse.class)
	static abstract class MixinMouseHack {
		private static boolean disableCursorDisabling = Boolean.parseBoolean(System.getProperty("Crawl.disableCursorDisabling", "false"));
		
		@ModifyConstant(method="lockCursor", constant = @Constant(intValue = GLFW.GLFW_CURSOR_DISABLED))
		public int onCursorParameter(int old) {
			return disableCursorDisabling ? GLFW.GLFW_CURSOR_NORMAL : old;
		}
	}
}
