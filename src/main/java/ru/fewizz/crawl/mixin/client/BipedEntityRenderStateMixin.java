package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import ru.fewizz.crawl.mixininterface.CrawlingState;

@Mixin(BipedEntityRenderState.class)
public class BipedEntityRenderStateMixin implements CrawlingState {

	@Unique
	boolean isCrawling = false;

	@Override
	public boolean isCrawling() {
		return this.isCrawling;
	}

	@Override
	public void setCrawling(boolean v) {
		this.isCrawling = v;
	}

}
