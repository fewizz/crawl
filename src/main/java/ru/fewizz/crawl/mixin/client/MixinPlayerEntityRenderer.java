package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import ru.fewizz.crawl.CrawlMod.Shared;

@Mixin(PlayerEntityRenderer.class)
abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	public MixinPlayerEntityRenderer(EntityRenderDispatcher entityRenderDispatcher_1,
			PlayerEntityModel<AbstractClientPlayerEntity> entityModel_1, float float_1) {
		super(entityRenderDispatcher_1, entityModel_1, float_1);
	}
	
	@Redirect(method="method_4215", at=@At(value="INVOKE", target="net/minecraft/client/network/AbstractClientPlayerEntity.isSneaking()Z"), require = 1)
	public boolean catchOnSneaking(AbstractClientPlayerEntity p) {
		if(p.getPose() == Shared.CRAWLING)
			return false;
		return p.isSneaking();
	}
}