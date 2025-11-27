package ru.fewizz.crawl.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import ru.fewizz.crawl.Crawl;

public class CrawlInitializer implements ModInitializer {

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(Crawl.Request.TYPE, Crawl.Request.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(Crawl.Request.TYPE, (payload, context) -> {
			Crawl.onCrawlRequestFromClient(context.player(), context.server(), payload.crawl());
		});

		Crawl.sendCrawlRequestPacketToServer = (wantsToCrawl) -> {
			ClientPlayNetworking.send(new Crawl.Request(wantsToCrawl));
		};
	}

}
