package ru.fewizz.theotherside.plugin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.fewizz.theotherside.CrawlMod;
import ru.fewizz.theotherside.CrawlMod.Client;
import ru.fewizz.theotherside.CrawlMod.Shared;


public class CrawlMixinPlugin implements IMixinConfigPlugin, Opcodes {
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
	
	/*@Mixin(Entity.class)
	public static abstract class MixinEntity {
		@Inject(method="setSneaking", at=@At("HEAD"))
		public void onSetSneaking(boolean val, CallbackInfo ci) {
			Object ths = this;
			if(ths instanceof PlayerEntity)
				Shared.onSetSneaking((PlayerEntity)ths, val);
		}
	}*/
	
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
		
		@Inject(method="updateMovement", at=@At(value="INVOKE", target="net/minecraft/client/network/AbstractClientPlayerEntity.updateMovement"))
		public void beforeSuperMovementUpdate(CallbackInfo ci) {
			Client.logic((ClientPlayerEntity)(Object)this);
		}
	}
	
	@Mixin(BipedEntityModel.class)
	public static class MixinBipedEntityModel {
		
		@SuppressWarnings("rawtypes")
		@Inject(method="method_17087", at=@At("RETURN"))
		void postSetAngles(LivingEntity e, float f1, float f2, float f3, float f4, float f5, float f6, CallbackInfo ci) {
			CrawlMod.Client.postTransformModel((BipedEntityModel)(Object)this, e, f1);
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
	
	@Override
    public List<String> getMixins() {
		return Lists.newArrayList(
        		"MixinPlayerEntity",
        		//"MixinEntity",
        		"MixinClientPlayerEntity",
        		"MixinBipedEntityModel",
        		"MixinMouseHack"
        ).stream().map(cn -> getClass().getSimpleName() + "$" + cn).collect(Collectors.toList());
    }
	
	@Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
	
	@Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
