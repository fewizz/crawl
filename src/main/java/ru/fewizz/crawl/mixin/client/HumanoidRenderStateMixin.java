package ru.fewizz.crawl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import ru.fewizz.crawl.mixininterface.CrawlingState;

@Mixin(HumanoidRenderState.class)
public class HumanoidRenderStateMixin implements CrawlingState {

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
