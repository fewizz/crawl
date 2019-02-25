package ru.fewizz.crawl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BoundingBox;
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
		getDataTracker().startTracking(CrawlMod.Shared.IS_CRAWLING, false);
	}
	
	@Inject(method="updateSize", at=@At("HEAD"), cancellable=true)
	public void onPreSetSize(CallbackInfo ci) {
		PlayerEntity p = (PlayerEntity)(Object)this;
		
		if(!Shared.isCrawling(p))
			return;
		
		ci.cancel();
		
		if(p.getPose() != CrawlMod.Shared.CRAWLING) {
			EntitySize size = CrawlMod.Shared.CRAWLING_SIZE;
			BoundingBox bb = this.getBoundingBox();
			bb = new BoundingBox(bb.minX, bb.minY, bb.minZ, bb.minX + size.width, bb.minY + size.height, bb.minZ + size.width);
			if (this.world.isEntityColliding(this, bb))
				this.setPose(Shared.CRAWLING);
		}
	}
	
	@Inject(method="updateSize", at=@At("HEAD"))
	public void onSetSize(CallbackInfo ci) {
		PlayerEntity p = (PlayerEntity)(Object)this;
		
		if(Shared.shouldCrawl(p))
			Shared.setCrawlingForce(p);
	}
	
	@Inject(method="getSize", at=@At("HEAD"), cancellable=true)
	public void onGetSize(EntityPose pose, CallbackInfoReturnable<EntitySize> ci) {		
		if(pose == CrawlMod.Shared.CRAWLING)
			ci.setReturnValue(CrawlMod.Shared.CRAWLING_SIZE);
	}
	
	@Inject(method="getActiveEyeHeight", at=@At("HEAD"), cancellable=true)
	public void onGetActiveEyeHeight(EntityPose entityPose_1, EntitySize entitySize_1, CallbackInfoReturnable<Float> ci) {
		if(entityPose_1 == CrawlMod.Shared.CRAWLING || entitySize_1 == CrawlMod.Shared.CRAWLING_SIZE)
			ci.setReturnValue(0.6F);
	}
}