package ru.fewizz.crawl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import ru.fewizz.crawl.CrawlMod;
import ru.fewizz.crawl.CrawlMod.Shared;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends Entity {

	public MixinPlayerEntity(EntityType<?> entityType_1, World world_1) {
		super(entityType_1, world_1);
	}

	@Inject(method="initDataTracker", at=@At("HEAD"))
	public void onInitDataDtracker(CallbackInfo ci) {
		getDataTracker().startTracking(CrawlMod.Shared.CRAWLING_REQUEST, false);
	}
	
	@Redirect(
		method="updateSize",
		at=@At(
			value="INVOKE",
			target = "net/minecraft/entity/player/PlayerEntity.setPose(Lnet/minecraft/entity/EntityPose;)V"
		),
		require = 1
		)
	public void onPreSetPose(PlayerEntity pl, EntityPose pose) {
		boolean replaceSwimming = pose == EntityPose.SWIMMING && !pl.isSwimming();
		boolean crawl = pl.getDataTracker().get(Shared.CRAWLING_REQUEST) && !pl.isSwimming();
		
		if((replaceSwimming || crawl) && !pl.isFallFlying())
			pose = Shared.CRAWLING;
		setPose(pose);
	}
	
	@Inject(method="getDimensions", at=@At("HEAD"), cancellable=true)
	public void onGetDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> ci) {
		if(pose == CrawlMod.Shared.CRAWLING)
			ci.setReturnValue(CrawlMod.Shared.CRAWLING_SIZE);
	}
	
	@Inject(method="getActiveEyeHeight", at=@At("HEAD"), cancellable=true)
	public void onGetActiveEyeHeight(EntityPose entityPose_1, EntityDimensions entitySize_1, CallbackInfoReturnable<Float> ci) {
		if(entityPose_1 == CrawlMod.Shared.CRAWLING || entitySize_1 == CrawlMod.Shared.CRAWLING_SIZE)
			ci.setReturnValue(0.6F);
	}
}